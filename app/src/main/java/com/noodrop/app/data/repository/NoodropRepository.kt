package com.noodrop.app.data.repository

import com.noodrop.app.data.firebase.FirebaseDataSource
import com.noodrop.app.data.model.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoodropRepository @Inject constructor(
    private val fb: FirebaseDataSource,
) {
    // ── Auth ──────────────────────────────────────────────────────────────────
    val authState: Flow<AuthState> = fb.authState

    suspend fun signIn(email: String, password: String) = fb.signIn(email, password)
    suspend fun signUp(email: String, password: String) = fb.signUp(email, password)
    fun signOut() = fb.signOut()

    // ── Stack ─────────────────────────────────────────────────────────────────
    fun stackFlow(): Flow<List<StackEntry>> = fb.stackFlow()

    suspend fun addToStack(entry: StackEntry) {
        fb.addStackEntry(entry)
        fb.addTimelineEntry("Added ${entry.compoundName} (${entry.dose}) - ${entry.timing.label}")
    }

    suspend fun removeFromStack(entry: StackEntry) {
        fb.removeStackEntry(entry.id)
        fb.addTimelineEntry("Removed ${entry.compoundName}")
    }

    suspend fun loadPreset(protocol: Protocol) {
        fb.replaceStack(protocol.presetEntries)
        fb.addTimelineEntry("Loaded ${protocol.name}")
    }

    // ── Logs ──────────────────────────────────────────────────────────────────
    fun logsFlow(days: Int): Flow<List<DayLog>> = fb.logsFlow(days)

    suspend fun upsertLog(log: DayLog) = fb.upsertLog(log)

    suspend fun getTodayLog(): DayLog? = fb.getLogByDate(LocalDate.now())

    // ── Notes ─────────────────────────────────────────────────────────────────
    fun notesFlow(): Flow<String> = fb.notesFlow()

    suspend fun saveNote(text: String) {
        fb.saveNote(text)
        fb.addTimelineEntry("Protocol notes updated")
    }

    // ── Timeline ──────────────────────────────────────────────────────────────
    fun timelineFlow(): Flow<List<TimelineEntry>> = fb.timelineFlow()

    // ── Streak ────────────────────────────────────────────────────────────────
    suspend fun computeStreak(logs: List<DayLog>, stackSize: Int): StreakInfo {
        val today  = LocalDate.now()
        val logMap = logs.associateBy { it.date }

        val last30 = (29 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val log  = logMap[date]
            when {
                log == null      -> DayStatus.NO_DATA
                log.completedAll -> DayStatus.DONE
                else             -> DayStatus.MISSED
            }
        }

        var streak = 0
        for (i in 0 until 60) {
            val log = logMap[today.minusDays(i.toLong())]
            if (log != null && log.completedAll) streak++ else if (i > 0) break
        }
        return StreakInfo(streak, last30)
    }

    // ── Analytics ─────────────────────────────────────────────────────────────
    suspend fun computeCompoundImpacts(days: Int): List<CompoundImpactScore> {
        val logs    = logsFlow(days).firstOrNull() ?: emptyList()
        val impacts = mutableMapOf<String, MutableList<IntArray>>()

        logs.filter { it.checkedCompounds.isNotEmpty() && (it.mood > 0 || it.fog > 0) }
            .forEach { log ->
                log.checkedCompounds.forEach { compound ->
                    impacts.getOrPut(compound) { mutableListOf() }
                        .add(intArrayOf(log.mood, log.fog, log.energy, log.focus))
                }
            }

        return impacts.map { (compound, values) ->
            val avgMood   = values.map { it[0] }.average().toFloat()
            val avgFog    = values.map { it[1] }.average().toFloat()
            val avgEnergy = values.map { it[2] }.average().toFloat()
            val avgFocus  = values.map { it[3] }.average().toFloat()

            CompoundImpactScore(
                compoundName    = compound,
                impactMood      = avgMood - 5f,
                impactFog       = -(avgFog - 5f),
                impactEnergy    = avgEnergy - 5f,
                impactFocus     = avgFocus - 5f,
                usageCount      = values.size,
                averageRating   = (avgMood / 10f) * 5f,
                confidenceScore = minOf(1f, values.size / 5f),
            )
        }.sortedByDescending { it.impactMood }
    }

    suspend fun analyzeBestWorstDays(days: Int): BestWorstDaysAnalysis {
        val logs = (logsFlow(days).firstOrNull() ?: emptyList()).filter { it.mood > 0 }

        if (logs.isEmpty()) return BestWorstDaysAnalysis(null, null, 0f, "→ No data yet", emptyList())

        val bestDay  = logs.maxByOrNull { it.mood }
        val worstDay = logs.minByOrNull { it.mood }
        val avgMood  = logs.map { it.mood }.average().toFloat()
        val trend    = when {
            logs.size > 1 && logs.last().mood > logs.first().mood -> "↑ Improving"
            logs.size > 1 && logs.last().mood < logs.first().mood -> "↓ Declining"
            else -> "→ Stable"
        }

        return BestWorstDaysAnalysis(
            bestDay            = bestDay,
            worstDay           = worstDay,
            averageMood        = avgMood,
            moodTrend          = trend,
            compoundsInBestDay = bestDay?.checkedCompounds ?: emptyList(),
        )
    }

    // ── Subscription ──────────────────────────────────────────────────────────
    fun subscriptionFlow(): Flow<SubscriptionStatus> = fb.subscriptionFlow()

    suspend fun updateSubscription(status: SubscriptionStatus) = fb.updateSubscription(status)

    suspend fun checkProtocolAccess(protocolId: String): Boolean {
        val subscription = subscriptionFlow().firstOrNull() ?: SubscriptionStatus()
        val protocol     = ProtocolData.all.find { it.id == protocolId }

        return when (protocol?.status) {
            ProtocolStatus.FREE        -> true
            ProtocolStatus.PAID        -> subscription.isActive && subscription.plan != SubscriptionPlan.FREE
            ProtocolStatus.COMING_SOON -> false
            null                       -> false
        }
    }

    suspend fun cancelSubscription(): PurchaseResult = try {
        fb.cancelSubscription()
    } catch (e: Exception) {
        PurchaseResult(false, e.message ?: "Cancellation failed")
    }

    // ── Products (guides — collection: products) ──────────────────────────────
    fun productsFlow(): Flow<List<Product>> = fb.productsFlow()

    suspend fun purchaseProduct(productId: String): PurchaseResult {
        return try {
            var product = fb.getProductById(productId)

            if (product == null) {
                val allProducts = fb.productsFlow().firstOrNull() ?: emptyList()
                product = allProducts.find { p ->
                    p.id.equals(productId, ignoreCase = true) ||
                    p.name.contains(productId, ignoreCase = true) ||
                    p.category.contains(productId, ignoreCase = true)
                }
            }

            if (product == null) return PurchaseResult(false, "Produkt nicht gefunden (ID: $productId)")
            if (!product.isactive) return PurchaseResult(false, "Dieses Produkt ist momentan nicht verfügbar.")

            fb.purchaseProduct(product)
        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Purchase failed")
        }
    }

    // ── AppProducts (protocol unlocks — collection: products-app) ────────────
    fun appProductsFlow(): Flow<List<AppProduct>> = fb.appProductsFlow()

    // Returns the AppProduct for a protocol if it exists in products-app
    suspend fun getAppProductForProtocol(protocolId: String): AppProduct? =
        fb.getAppProductForProtocol(protocolId)

    // Single protocol purchase (€2.99)
    // Purchases via products-app collection.
    // productDocId = the Firestore document ID (e.g. "premium-subscription", "focus", "longevity")
    suspend fun purchaseAppProduct(productDocId: String): PurchaseResult {
        return try {
            val appProduct = fb.getAppProductForProtocol(productDocId)
                ?: return PurchaseResult(
                    false,
                    "Produkt '$productDocId' nicht in 'products-app' gefunden. Prüfe die Dokument-ID in Firestore."
                )
            if (!appProduct.isActive) return PurchaseResult(false, "Dieses Produkt ist momentan nicht erhältlich.")
            fb.purchaseAppProduct(appProduct)
        } catch (e: Exception) {
            PurchaseResult(false, e.message ?: "Purchase failed")
        }
    }
}
