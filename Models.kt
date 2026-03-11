package com.noodrop.app.data.model

import java.time.LocalDate

// -
// Compound
// -
data class ResearchLink(
    val title: String,
    val url: String,  // PubMed, Scholar, etc.
    val authors: String = "",
    val year: Int = 0,
)

data class Compound(
    val name: String,
    val category: String,
    val defaultDose: String,
    // Scientific metadata
    val description: String = "",
    val benefits: List<String> = emptyList(),  // e.g., ["Focus", "Memory", "Clarity"]
    val bioavailability: String = "Unknown",   // e.g., "95%" or "Moderate with fat"
    val halfLife: String = "Unknown",          // e.g., "3-5 hours"
    val optimalTiming: String = "Morning",     // Best time of day
    val foodInteraction: String = "",          // e.g., "Take with fat for absorption"
    val researchLinks: List<ResearchLink> = emptyList(),
    val safetyNotes: String = "",
    val synergies: List<String> = emptyList(), // Compounds that work well together
)

// -
// Stack entry
// -
data class StackEntry(
    val id: String         = "",      // Firestore document ID
    val compoundName: String,
    val dose: String,
    val timing: Timing,
    val sortOrder: Int     = 0,
)

enum class Timing(val label: String) {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening"),
    PRE_WORKOUT("Pre-workout"),
    WITH_FOOD("With food");

    companion object {
        fun fromLabel(l: String) = entries.firstOrNull { it.label == l } ?: MORNING
    }
}

// -
// Day log
// -
data class DayLog(
    val id: String                   = "", // date string "2025-03-07"
    val date: LocalDate,
    val mood: Int       = 0,
    val fog: Int        = 0,
    val energy: Int     = 0,
    val focus: Int      = 0,
    val health: Int     = 0,
    val notes: String   = "",
    val checkedCompounds: List<String> = emptyList(),
    val stackSize: Int  = 0,
) {
    val completedAll: Boolean
        get() = stackSize > 0 && checkedCompounds.size >= stackSize
}

// -
// Timeline entry
// -
data class TimelineEntry(
    val id: String      = "",
    val dateLabel: String,
    val text: String,
    val timestampMs: Long = System.currentTimeMillis(),
)

// -
// Protocol
// -
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
    val detailText: String,
    val presetEntries: List<StackEntry> = emptyList(),
)

// -
// Streak / consistency
// -
enum class DayStatus { DONE, MISSED, NO_DATA }

data class StreakInfo(
    val current: Int,
    val last30: List<DayStatus>,  // index 0 = 29 days ago, last = today
)

// -
// Auth state
// -
sealed class AuthState {
    object Loading    : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val uid: String, val email: String?) : AuthState()
}

// NEW: Compound Impact & Correlation Models
data class CompoundImpactScore(
    val compoundName: String,
    val impactMood: Float,           // -10 to +10
    val impactFog: Float,            // negative = better
    val impactEnergy: Float,
    val impactFocus: Float,
    val usageCount: Int,
    val averageRating: Float,        // 1-5 based on mood
    val confidenceScore: Float,      // How confident we are (based on usage count)
)

data class BestWorstDaysAnalysis(
    val bestDay: DayLog?,
    val worstDay: DayLog?,
    val averageMood: Float,
    val moodTrend: String,           // "↑ Improving" / "↓ Declining" / "→ Stable"
    val compoundsInBestDay: List<String>,
)

// NEW: Subscription & Payment Models
data class SubscriptionStatus(
    val isActive: Boolean = false,
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val expiresAt: Long? = null,  // timestamp
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val stripeCustomerId: String? = null,
)

enum class SubscriptionPlan(val displayName: String, val price: String) {
    FREE("Free", "Free"),
    PREMIUM("Premium", "€9.99/mo"),
    PRO("Pro", "€19.99/mo"),
}

enum class PaymentMethod {
    NONE, STRIPE, FIREBASE
}

data class Product(
    val id: String = "",
    val category: String = "",
    val description: String = "",
    val downloadurl: String = "",
    val features: List<String> = emptyList(),
    val image: String = "",
    val isactive: Boolean = true,
    val name: String = "",
    val price: Double = 0.0,
    val priceformatted: String = "",
)


data class PurchaseResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val transactionId: String? = null,
    val sessionId: String? = null,  // ← NEW: For Stripe checkout
    val downloadUrl: String? = null,  // ← NEW: For free downloads
)
