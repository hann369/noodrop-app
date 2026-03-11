package com.noodrop.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions  // ← NEW
import com.noodrop.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore schema (per user):
 *
 * users/{uid}/
 *   stack/{docId}         - StackEntry documents
 *   logs/{dateString}     - DayLog documents  (id == "2025-03-07")
 *   meta/notes            - { text: String }
 *   timeline/{docId}      - TimelineEntry documents
 */
@Singleton
class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db:   FirebaseFirestore,
) {
    // Firebase Functions for Stripe
    private val functions = FirebaseFunctions.getInstance()  // ← NEW

    // - helpers -
    private val uid: String get() = auth.currentUser?.uid
        ?: error("User not authenticated")

    private fun userCol(col: String) = db.collection("users").document(uid).collection(col)

    // - Auth stream -
    val authState: Flow<AuthState> = callbackFlow {
        trySend(AuthState.Loading)
        val listener = FirebaseAuth.AuthStateListener { a ->
            val user = a.currentUser
            trySend(
                if (user != null) AuthState.Authenticated(user.uid, user.email)
                else AuthState.Unauthenticated
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    suspend fun signIn(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password).await()

    suspend fun signUp(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = result.user!!.uid
        // Create user document
        db.collection("users").document(uid)
            .set(mapOf("email" to email, "createdAt" to com.google.firebase.Timestamp.now()), SetOptions.merge())
            .await()
        return uid
    }

    fun signOut() = auth.signOut()

    // - Stack -
    fun stackFlow(): Flow<List<StackEntry>> = callbackFlow {
        val reg = userCol("stack")
            .orderBy("sortOrder")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toStackEntry()
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addStackEntry(entry: StackEntry): String {
        val ref = userCol("stack").document()
        ref.set(entry.toMap()).await()
        return ref.id
    }

    suspend fun removeStackEntry(id: String) =
        userCol("stack").document(id).delete().await()

    suspend fun replaceStack(entries: List<StackEntry>) {
        val col = userCol("stack")
        // Delete all then batch-write
        val existing = col.get().await()
        db.runBatch { batch ->
            existing.documents.forEach { batch.delete(it.reference) }
            entries.forEachIndexed { i, e ->
                val ref = col.document()
                batch.set(ref, e.copy(sortOrder = i).toMap())
            }
        }.await()
    }

    // - Day Logs -
    fun logsFlow(days: Int): Flow<List<DayLog>> = callbackFlow {
        val from = LocalDate.now().minusDays(days.toLong() - 1).toString()
        val reg = userCol("logs")
            .whereGreaterThanOrEqualTo("date", from)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.toDayLog() } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun upsertLog(log: DayLog) {
        val dateKey = log.date.toString()
        userCol("logs").document(dateKey).set(log.toMap(), SetOptions.merge()).await()
    }

    suspend fun getLogByDate(date: LocalDate): DayLog? =
        userCol("logs").document(date.toString()).get().await().toDayLog()

    // - Notes -
    fun notesFlow(): Flow<String> = callbackFlow {
        val reg = userCol("meta").document("notes")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.getString("text") ?: "")
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveNote(text: String) =
        userCol("meta").document("notes").set(mapOf("text" to text), SetOptions.merge()).await()

    // - Timeline -
    fun timelineFlow(): Flow<List<TimelineEntry>> = callbackFlow {
        val reg = userCol("timeline")
            .orderBy("timestampMs", Query.Direction.DESCENDING)
            .limit(15)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { doc ->
                    TimelineEntry(
                        id          = doc.id,
                        dateLabel   = doc.getString("dateLabel") ?: "",
                        text        = doc.getString("text") ?: "",
                        timestampMs = doc.getLong("timestampMs") ?: 0L,
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addTimelineEntry(text: String) {
        val label = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))
        userCol("timeline").document().set(
            mapOf("dateLabel" to label, "text" to text, "timestampMs" to System.currentTimeMillis())
        ).await()
    }

    // NEW STEP 3: SUBSCRIPTION & PAYMENT METHODS
    // - Subscription management -
    fun subscriptionFlow(): Flow<SubscriptionStatus> = callbackFlow {
        val reg = userCol("meta").document("subscription")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val status = snap?.toSubscriptionStatus() ?: SubscriptionStatus()
                trySend(status)
            }
        awaitClose { reg.remove() }
    }

    suspend fun updateSubscription(status: SubscriptionStatus) {
        userCol("meta").document("subscription").set(status.toMap(), SetOptions.merge()).await()
    }

    // - Product management -
    fun productsFlow(): Flow<List<Product>> = callbackFlow {
        val reg = db.collection("products")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val products = snap?.documents?.mapNotNull { doc ->
                    doc.toProduct()
                } ?: emptyList()
                trySend(products)
            }
        awaitClose { reg.remove() }
    }

    suspend fun purchaseProduct(product: Product): PurchaseResult {
        return try {
            // Check if product is free
            if (product.price <= 0.0) {
                // Free product - just provide download URL
                return PurchaseResult(
                    success = true,
                    transactionId = "free_${product.id}_${System.currentTimeMillis()}",
                    downloadUrl = product.downloadurl
                )
            }

            // Paid product - create Stripe checkout session via Firebase Functions
            val data = mapOf(
                "productId" to product.id,
                "productName" to product.name,
                "amount" to (product.price * 100).toInt(), // Convert to cents
                "currency" to "eur",
                "userId" to uid,
                "userEmail" to (auth.currentUser?.email ?: "")
            )

            val result = functions
                .getHttpsCallable("createStripeCheckoutSession")
                .call(data)
                .await()

            val sessionData = result.data as? Map<*, *>
            val sessionId = sessionData?.get("sessionId") as? String
                ?: return PurchaseResult(false, "No session ID received")

            // Return session ID for Stripe SDK to handle
            PurchaseResult(
                success = true,
                transactionId = sessionId,
                sessionId = sessionId
            )

        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Purchase failed")
        }
    }

    suspend fun cancelSubscription(): PurchaseResult {
        return try {
            val cancelledStatus = SubscriptionStatus(
                isActive = false,
                plan = SubscriptionPlan.FREE,
                paymentMethod = PaymentMethod.NONE,
                expiresAt = null
            )
            updateSubscription(cancelledStatus)
            PurchaseResult(true, transactionId = "cancel_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Cancellation failed")
        }
    }
}

// - Firestore ? Domain mappers -

private fun StackEntry.toMap() = mapOf(
    "compoundName" to compoundName,
    "dose"         to dose,
    "timing"       to timing.name,
    "sortOrder"    to sortOrder,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toStackEntry(): StackEntry? {
    return try {
        StackEntry(
            id           = id,
            compoundName = getString("compoundName") ?: return null,
            dose         = getString("dose") ?: "",
            timing       = Timing.fromLabel(getString("timing") ?: ""),
            sortOrder    = getLong("sortOrder")?.toInt() ?: 0,
        )
    } catch (e: Exception) { null }
}

private fun DayLog.toMap() = mapOf(
    "date"              to date.toString(),
    "mood"              to mood,
    "fog"               to fog,
    "energy"            to energy,
    "focus"             to focus,
    "health"            to health,
    "notes"             to notes,
    "checkedCompounds"  to checkedCompounds,
    "stackSize"         to stackSize,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toDayLog(): DayLog? {
    return try {
        val dateStr = getString("date") ?: return null
        @Suppress("UNCHECKED_CAST")
        DayLog(
            id                = id,
            date              = LocalDate.parse(dateStr),
            mood              = getLong("mood")?.toInt()   ?: 0,
            fog               = getLong("fog")?.toInt()    ?: 0,
            energy            = getLong("energy")?.toInt() ?: 0,
            focus             = getLong("focus")?.toInt()  ?: 0,
            health            = getLong("health")?.toInt() ?: 0,
            notes             = getString("notes") ?: "",
            checkedCompounds  = (get("checkedCompounds") as? List<String>) ?: emptyList(),
            stackSize         = getLong("stackSize")?.toInt() ?: 0,
        )
    } catch (e: Exception) { null }
}

// NEW STEP 3: SUBSCRIPTION & PRODUCT MAPPERS
private fun SubscriptionStatus.toMap() = mapOf(
    "isActive"       to isActive,
    "plan"           to plan.name,
    "expiresAt"      to expiresAt,
    "paymentMethod"  to paymentMethod.name,
    "stripeCustomerId" to stripeCustomerId,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toSubscriptionStatus(): SubscriptionStatus? {
    return try {
        SubscriptionStatus(
            isActive        = getBoolean("isActive") ?: false,
            plan            = SubscriptionPlan.valueOf(getString("plan") ?: "FREE"),
            expiresAt       = getLong("expiresAt"),
            paymentMethod   = PaymentMethod.valueOf(getString("paymentMethod") ?: "NONE"),
            stripeCustomerId = getString("stripeCustomerId"),
        )
    } catch (e: Exception) { null }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(): Product? {
    return try {
        @Suppress("UNCHECKED_CAST")
        Product(
            id = id,
            category = getString("category") ?: "",
            description = getString("description") ?: "",
            downloadurl = getString("downloadurl") ?: "",
            features = (get("features") as? List<String>) ?: emptyList(),
            image = getString("image") ?: "",
            isactive = getBoolean("isactive") ?: true,
            name = getString("name") ?: return null,
            price = (get("price") as? Number)?.toDouble() ?: 0.0,
            priceformatted = getString("priceformatted") ?: "",
        )
    } catch (e: Exception) { null }
}
