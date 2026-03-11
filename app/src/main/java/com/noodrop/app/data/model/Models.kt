package com.noodrop.app.data.model

import java.time.LocalDate

// ── Research ──────────────────────────────────────────────────────────────────
data class ResearchLink(
    val title: String,
    val url: String,
    val author: String,
    val year: Int,
) {
    // Alias for CompoundDetailSheet compatibility
    val authors: String get() = author
}

// ── Compound ──────────────────────────────────────────────────────────────────
data class Compound(
    val name: String,
    val category: String,
    val defaultDose: String,
    val description: String         = "",
    val benefits: List<String>      = emptyList(),
    val bioavailability: String     = "",
    val halfLife: String            = "",
    val optimalTiming: String       = "",
    val foodInteraction: String     = "",
    val safetyNotes: String         = "",
    val synergies: List<String>     = emptyList(),
    val researchLinks: List<ResearchLink> = emptyList(),
)

// ── Stack ─────────────────────────────────────────────────────────────────────
enum class Timing(val label: String) {
    MORNING("Morning"), AFTERNOON("Afternoon"), EVENING("Evening"), ANYTIME("Anytime");

    companion object {
        fun fromLabel(s: String) = values().firstOrNull { it.label == s } ?: MORNING
    }
}

data class StackEntry(
    val id: String           = "",
    val compoundName: String = "",
    val dose: String         = "",
    val timing: Timing       = Timing.MORNING,
    val sortOrder: Int       = 0,
)

// ── Log ───────────────────────────────────────────────────────────────────────
data class DayLog(
    val id: String                   = "",
    val date: LocalDate              = LocalDate.now(),
    val mood: Int                    = 0,
    val fog: Int                     = 0,
    val energy: Int                  = 0,
    val focus: Int                   = 0,
    val health: Int                  = 0,
    val notes: String                = "",
    val checkedCompounds: List<String> = emptyList(),
    val stackSize: Int               = 0,
) {
    val completedAll: Boolean get() = stackSize > 0 && checkedCompounds.size >= stackSize
}

enum class DayStatus { DONE, MISSED, NO_DATA }

// ── Timeline ──────────────────────────────────────────────────────────────────
data class TimelineEntry(
    val id: String          = "",
    val dateLabel: String   = "",
    val text: String        = "",
    val timestampMs: Long   = 0L,
)

// ── Protocol ──────────────────────────────────────────────────────────────────
enum class ProtocolStatus { FREE, PAID, COMING_SOON }
enum class ProtocolAccent { ORANGE, GREEN, BLUE, PURPLE }

data class Protocol(
    val id: String,
    val name: String,
    val icon: String,
    val accent: ProtocolAccent,
    val status: ProtocolStatus,
    val description: String,
    val compounds: List<String>,
    val goal: String,
    val duration: String,
    val price: String,
    val detailText: String       = "",
    val presetEntries: List<StackEntry> = emptyList(),
    // Links to the products-app Firestore document ID for single purchase
    val appProductId: String     = "",
)

// ── Streak ────────────────────────────────────────────────────────────────────
data class StreakInfo(
    val currentStreak: Int,
    val last30Days: List<DayStatus>,
) {
    // Aliases for DashboardViewModel / ProfileSheet compatibility
    val current: Int get() = currentStreak
    val last30: List<DayStatus> get() = last30Days
}

// ── Auth ──────────────────────────────────────────────────────────────────────
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val uid: String, val email: String?) : AuthState()
}

// ── Analytics ─────────────────────────────────────────────────────────────────
data class CompoundImpactScore(
    val compoundName: String,
    val impactMood: Float,
    val impactFog: Float,
    val impactEnergy: Float,
    val impactFocus: Float,
    val usageCount: Int,
    val averageRating: Float,
    val confidenceScore: Float,
)

data class BestWorstDaysAnalysis(
    val bestDay: DayLog?,
    val worstDay: DayLog?,
    val averageMood: Float,
    val moodTrend: String,
    val compoundsInBestDay: List<String>,
)

// ── Subscription ──────────────────────────────────────────────────────────────
data class SubscriptionStatus(
    val isActive: Boolean               = false,
    val plan: SubscriptionPlan          = SubscriptionPlan.FREE,
    val expiresAt: Long?                = null,
    val paymentMethod: PaymentMethod    = PaymentMethod.NONE,
    val stripeCustomerId: String?       = null,
)

// PRO plan removed - only FREE and PREMIUM
enum class SubscriptionPlan(val displayName: String, val price: String) {
    FREE("Free", "Free"),
    PREMIUM("Premium", "€9.99/mo"),
}

enum class PaymentMethod { NONE, STRIPE, GOOGLE_PAY }

// ── Products (Firestore: products collection - guides etc.) ───────────────────
data class Product(
    val id: String              = "",
    val category: String        = "",
    val description: String     = "",
    val downloadurl: String     = "",
    val features: List<String>  = emptyList(),
    val image: String           = "",
    val isactive: Boolean       = true,
    val name: String            = "",
    val price: Double           = 0.0,
    val priceformatted: String  = "",
)

// ── AppProducts (Firestore: products-app collection - protocol unlocks) ───────
data class AppProduct(
    val id: String              = "",
    val protocolId: String      = "",   // matches Protocol.id
    val name: String            = "",
    val description: String     = "",
    val price: Double           = 2.99,
    val priceformatted: String  = "€2.99",
    val downloadURL: String     = "",   // Stripe Payment Link
    val isActive: Boolean       = true,
)

// ── Purchase ──────────────────────────────────────────────────────────────────
data class PurchaseResult(
    val success: Boolean,
    val errorMessage: String?   = null,
    val transactionId: String?  = null,
    val sessionId: String?      = null,
    val downloadUrl: String?    = null,
)
