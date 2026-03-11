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
    // - Auth -
    val authState: Flow<AuthState> = fb.authState

    suspend fun signIn(email: String, password: String) = fb.signIn(email, password)
    suspend fun signUp(email: String, password: String) = fb.signUp(email, password)
    fun signOut() = fb.signOut()

    // - Stack -
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

    // - Logs -
    fun logsFlow(days: Int): Flow<List<DayLog>> = fb.logsFlow(days)

    suspend fun upsertLog(log: DayLog) = fb.upsertLog(log)

    suspend fun getTodayLog(): DayLog? = fb.getLogByDate(LocalDate.now())

    // - Notes -
    fun notesFlow(): Flow<String> = fb.notesFlow()

    suspend fun saveNote(text: String) {
        fb.saveNote(text)
        fb.addTimelineEntry("Protocol notes updated")
    }

    // - Timeline -
    fun timelineFlow(): Flow<List<TimelineEntry>> = fb.timelineFlow()

    // - Streak -
    suspend fun computeStreak(logs: List<DayLog>, stackSize: Int): StreakInfo {
        val today = LocalDate.now()
        val logMap = logs.associateBy { it.date }

        val last30 = (29 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val log  = logMap[date]
            when {
                log == null        -> DayStatus.NO_DATA
                log.completedAll   -> DayStatus.DONE
                else               -> DayStatus.MISSED
            }
        }

        var streak = 0
        for (i in 0 until 60) {
            val log = logMap[today.minusDays(i.toLong())]
            if (log != null && log.completedAll) streak++ else if (i > 0) break
        }
        return StreakInfo(streak, last30)
    }

    // NEW STEP 2 FUNCTIONS
    // - Compute compound impact scores -
    suspend fun computeCompoundImpacts(days: Int): List<CompoundImpactScore> {
        val logs = logsFlow(days).first()
        val stack = stackFlow().first()

        val impacts = mutableMapOf<String, MutableList<IntArray>>()  // [mood, fog, energy, focus]

        // For each day, calculate impact of compounds
        logs.filter { it.checkedCompounds.isNotEmpty() && (it.mood > 0 || it.fog > 0) }.forEach { log ->
            log.checkedCompounds.forEach { compound ->
                if (impacts[compound] == null) impacts[compound] = mutableListOf()
                impacts[compound]!!.add(intArrayOf(log.mood, log.fog, log.energy, log.focus))
            }
        }

        return impacts.map { (compound, values) ->
            val avgMood = values.map { it[0] }.average().toFloat()
            val avgFog = values.map { it[1] }.average().toFloat()
            val avgEnergy = values.map { it[2] }.average().toFloat()
            val avgFocus = values.map { it[3] }.average().toFloat()

            // Confidence is higher with more data points
            val confidence = minOf(1f, (values.size / 5f))

            CompoundImpactScore(
                compoundName = compound,
                impactMood = avgMood - 5f,  // baseline 5
                impactFog = -(avgFog - 5f),  // lower fog is better (negative impact is good)
                impactEnergy = avgEnergy - 5f,
                impactFocus = avgFocus - 5f,
                usageCount = values.size,
                averageRating = (avgMood / 10f) * 5f,  // convert to 1-5
                confidenceScore = confidence,
            )
        }.sortedByDescending { it.impactMood }  // Sort by mood impact
    }

    // - Analyze best and worst days -
    suspend fun analyzeBestWorstDays(days: Int): BestWorstDaysAnalysis {
        val logs = logsFlow(days).first().filter { it.mood > 0 }

        if (logs.isEmpty()) {
            return BestWorstDaysAnalysis(
                bestDay = null,
                worstDay = null,
                averageMood = 0f,
                moodTrend = "→ No data yet",
                compoundsInBestDay = emptyList(),
            )
        }

        val bestDay = logs.maxByOrNull { it.mood }
        val worstDay = logs.minByOrNull { it.mood }
        val avgMood = logs.map { it.mood }.average().toFloat()

        val trend = when {
            logs.size > 1 && logs.last().mood > logs.first().mood -> "↑ Improving"
            logs.size > 1 && logs.last().mood < logs.first().mood -> "↓ Declining"
            else -> "→ Stable"
        }

        return BestWorstDaysAnalysis(
            bestDay = bestDay,
            worstDay = worstDay,
            averageMood = avgMood,
            moodTrend = trend,
            compoundsInBestDay = bestDay?.checkedCompounds ?: emptyList(),
        )
    }

    // NEW STEP 3 FUNCTIONS - SUBSCRIPTIONS & PAYMENTS
    // - Subscription management -
    fun subscriptionFlow(): Flow<SubscriptionStatus> = fb.subscriptionFlow()

    suspend fun updateSubscription(status: SubscriptionStatus) = fb.updateSubscription(status)

    suspend fun checkProtocolAccess(protocolId: String): Boolean {
        val subscription = subscriptionFlow().first()
        val protocol = ProtocolData.all.find { it.id == protocolId }

        return when (protocol?.status) {
            ProtocolStatus.FREE -> true
            ProtocolStatus.PAID -> subscription.isActive && subscription.plan != SubscriptionPlan.FREE
            ProtocolStatus.COMING_SOON -> false
            null -> false
        }
    }

    // - Product management -
    fun productsFlow(): Flow<List<Product>> = fb.productsFlow()

    suspend fun purchaseProduct(productId: String): PurchaseResult {
        val product = productsFlow().first().find { it.id == productId }
        return product?.let { fb.purchaseProduct(it) } ?: PurchaseResult(false, "Product not found")
    }

    // - Stripe integration (placeholder for future) -
    suspend fun createStripeSubscription(priceId: String): PurchaseResult {
        // TODO: Implement Stripe integration
        return PurchaseResult(false, "Stripe integration not yet implemented")
    }

    suspend fun cancelSubscription(): PurchaseResult {
        return fb.cancelSubscription()
    }
}
