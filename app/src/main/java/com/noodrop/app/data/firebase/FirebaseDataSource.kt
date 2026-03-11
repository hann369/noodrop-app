package com.noodrop.app.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.noodrop.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db:   FirebaseFirestore,
) {
    // ── Helpers ───────────────────────────────────────────────────────────────
    private val uid: String
        get() = auth.currentUser?.uid ?: error("User not authenticated")

    private fun userCol(col: String) =
        db.collection("users").document(uid).collection(col)

    // ── Auth ──────────────────────────────────────────────────────────────────
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
        val newUid = result.user!!.uid
        db.collection("users").document(newUid)
            .set(mapOf("email" to email, "createdAt" to com.google.firebase.Timestamp.now()), SetOptions.merge())
            .await()
        return newUid
    }

    fun signOut() = auth.signOut()

    // ── Stack ─────────────────────────────────────────────────────────────────
    fun stackFlow(): Flow<List<StackEntry>> = callbackFlow {
        val reg = userCol("stack")
            .orderBy("sortOrder")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toStackEntry() } ?: emptyList())
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
        val col      = userCol("stack")
        val existing = col.get().await()
        db.runBatch { batch ->
            existing.documents.forEach { batch.delete(it.reference) }
            entries.forEachIndexed { i, e ->
                batch.set(col.document(), e.copy(sortOrder = i).toMap())
            }
        }.await()
    }

    // ── Logs ──────────────────────────────────────────────────────────────────
    fun logsFlow(days: Int): Flow<List<DayLog>> = callbackFlow {
        val from = LocalDate.now().minusDays(days.toLong() - 1).toString()
        val reg  = userCol("logs")
            .whereGreaterThanOrEqualTo("date", from)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toDayLog() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun upsertLog(log: DayLog) =
        userCol("logs").document(log.date.toString()).set(log.toMap(), SetOptions.merge()).await()

    suspend fun getLogByDate(date: LocalDate): DayLog? =
        userCol("logs").document(date.toString()).get().await().toDayLog()

    // ── Notes ─────────────────────────────────────────────────────────────────
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

    // ── Timeline ──────────────────────────────────────────────────────────────
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

    // ── Subscription ──────────────────────────────────────────────────────────
    fun subscriptionFlow(): Flow<SubscriptionStatus> = callbackFlow {
        val reg = userCol("meta").document("subscription")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toSubscriptionStatus() ?: SubscriptionStatus())
            }
        awaitClose { reg.remove() }
    }

    suspend fun updateSubscription(status: SubscriptionStatus) =
        userCol("meta").document("subscription").set(status.toMap(), SetOptions.merge()).await()

    suspend fun cancelSubscription(): PurchaseResult {
        return try {
            updateSubscription(SubscriptionStatus(isActive = false, plan = SubscriptionPlan.FREE))
            PurchaseResult(true, transactionId = "cancel_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Cancellation failed")
        }
    }

    // ── Products (guides/downloads — collection: "products") ──────────────────
    fun productsFlow(): Flow<List<Product>> = callbackFlow {
        val reg = db.collection("products")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toProduct() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            db.collection("products").document(productId).get().await().toProduct()
        } catch (e: Exception) { null }
    }

    suspend fun purchaseProduct(product: Product): PurchaseResult {
        return try {
            if (product.price <= 0.0) {
                return PurchaseResult(
                    success       = true,
                    transactionId = "free_${product.id}_${System.currentTimeMillis()}",
                    downloadUrl   = product.downloadurl.takeIf { it.isNotBlank() },
                )
            }
            val paymentUrl = product.downloadurl.takeIf { it.isNotBlank() }
                ?: return PurchaseResult(false, "No payment link configured for this product")
            PurchaseResult(
                success       = true,
                transactionId = "pending_${product.id}_${System.currentTimeMillis()}",
                downloadUrl   = paymentUrl,
            )
        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Purchase failed")
        }
    }

    // ── AppProducts (protocol unlocks — collection: "products-app") ───────────
    fun appProductsFlow(): Flow<List<AppProduct>> = callbackFlow {
        val reg = db.collection("products-app")
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toAppProduct() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    // Fetches the AppProduct for a given protocol.
    // Tries direct doc lookup (protocolId as doc ID), then falls back to a field query.
    suspend fun getAppProductForProtocol(protocolId: String): AppProduct? {
        return try {
            val direct = db.collection("products-app").document(protocolId).get().await()
            if (direct.exists()) return direct.toAppProduct()

            val query = db.collection("products-app")
                .whereEqualTo("protocolId", protocolId)
                .limit(1)
                .get()
                .await()
            query.documents.firstOrNull()?.toAppProduct()
        } catch (e: Exception) { null }
    }

    suspend fun purchaseAppProduct(appProduct: AppProduct): PurchaseResult {
        return try {
            val paymentUrl = appProduct.downloadURL.takeIf { it.isNotBlank() }
                ?: return PurchaseResult(
                    false,
                    "Kein Zahlungslink konfiguriert. Bitte trage eine Stripe Payment Link URL im Feld 'downloadURL' in Firestore ein (products-app/${appProduct.id})."
                )
            PurchaseResult(
                success       = true,
                transactionId = "pending_${appProduct.id}_${System.currentTimeMillis()}",
                downloadUrl   = paymentUrl,
            )
        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Purchase failed")
        }
    }
}

// ── Mappers ───────────────────────────────────────────────────────────────────
private fun StackEntry.toMap() = mapOf(
    "compoundName" to compoundName,
    "dose"         to dose,
    "timing"       to timing.name,
    "sortOrder"    to sortOrder,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toStackEntry(): StackEntry? {
    return try {
        val name = getString("compoundName") ?: return null
        StackEntry(
            id           = id,
            compoundName = name,
            dose         = getString("dose") ?: "",
            timing       = Timing.fromLabel(getString("timing") ?: ""),
            sortOrder    = getLong("sortOrder")?.toInt() ?: 0,
        )
    } catch (e: Exception) { null }
}

private fun DayLog.toMap() = mapOf(
    "date"             to date.toString(),
    "mood"             to mood,
    "fog"              to fog,
    "energy"           to energy,
    "focus"            to focus,
    "health"           to health,
    "notes"            to notes,
    "checkedCompounds" to checkedCompounds,
    "stackSize"        to stackSize,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toDayLog(): DayLog? {
    return try {
        val dateStr = getString("date") ?: return null
        @Suppress("UNCHECKED_CAST")
        DayLog(
            id               = id,
            date             = LocalDate.parse(dateStr),
            mood             = getLong("mood")?.toInt()    ?: 0,
            fog              = getLong("fog")?.toInt()     ?: 0,
            energy           = getLong("energy")?.toInt()  ?: 0,
            focus            = getLong("focus")?.toInt()   ?: 0,
            health           = getLong("health")?.toInt()  ?: 0,
            notes            = getString("notes") ?: "",
            checkedCompounds = (get("checkedCompounds") as? List<String>) ?: emptyList(),
            stackSize        = getLong("stackSize")?.toInt() ?: 0,
        )
    } catch (e: Exception) { null }
}

private fun SubscriptionStatus.toMap() = mapOf(
    "isActive"         to isActive,
    "plan"             to plan.name,
    "expiresAt"        to expiresAt,
    "paymentMethod"    to paymentMethod.name,
    "stripeCustomerId" to stripeCustomerId,
)

private fun com.google.firebase.firestore.DocumentSnapshot.toSubscriptionStatus(): SubscriptionStatus {
    return try {
        SubscriptionStatus(
            isActive         = getBoolean("isActive") ?: false,
            plan             = try { SubscriptionPlan.valueOf(getString("plan") ?: "FREE") } catch (e: Exception) { SubscriptionPlan.FREE },
            expiresAt        = getLong("expiresAt"),
            paymentMethod    = try { PaymentMethod.valueOf(getString("paymentMethod") ?: "NONE") } catch (e: Exception) { PaymentMethod.NONE },
            stripeCustomerId = getString("stripeCustomerId"),
        )
    } catch (e: Exception) { SubscriptionStatus() }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toProduct(): Product? {
    return try {
        @Suppress("UNCHECKED_CAST")
        Product(
            id             = id,
            category       = getString("category") ?: "",
            description    = getString("description") ?: "",
            // Support both casing variants
            downloadurl    = getString("downloadURL") ?: getString("downloadurl") ?: "",
            features       = (get("features") as? List<String>) ?: emptyList(),
            image          = getString("image") ?: "",
            isactive       = getBoolean("isActive") ?: getBoolean("isactive") ?: true,
            name           = getString("name") ?: return null,
            price          = (get("price") as? Number)?.toDouble() ?: 0.0,
            priceformatted = getString("priceFormatted") ?: getString("priceformatted") ?: "",
        )
    } catch (e: Exception) { null }
}

@Suppress("UNCHECKED_CAST")
private fun com.google.firebase.firestore.DocumentSnapshot.toAppProduct(): AppProduct? {
    return try {
        AppProduct(
            id             = id,
            protocolId     = getString("protocolId") ?: id,
            name           = getString("name") ?: "",
            description    = getString("description") ?: "",
            price          = (get("price") as? Number)?.toDouble() ?: 2.99,
            priceformatted = getString("priceFormatted") ?: getString("priceformatted") ?: "€2.99",
            downloadURL    = getString("downloadURL") ?: getString("downloadurl") ?: "",
            isActive       = getBoolean("isActive") ?: getBoolean("isactive") ?: true,
        )
    } catch (e: Exception) { null }
}
