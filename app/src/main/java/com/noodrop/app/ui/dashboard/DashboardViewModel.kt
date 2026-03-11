package com.noodrop.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DashboardState(
    val greeting: String = "",
    val dateLabel: String = "",
    val streak: Int = 0,
    val todayMood: String = "-",
    val todayFog: String = "-",
    val stackSize: Int = 0,
    val stack: List<StackEntry> = emptyList(),
    val checkedToday: Set<String> = emptySet(),
    val streakDays: List<DayStatus> = emptyList(),
    val recentLogs: List<DayLog> = emptyList(),
    val isLoading: Boolean = true,
    // NEW: Evidence-based suggestions
    val suggestions: List<CompoundSuggestion> = emptyList(),
)

// NEW: Suggestion model
data class CompoundSuggestion(
    val compound: Compound,
    val reason: String,  // e.g., "May improve low focus scores"
    val confidence: Float,  // 0.0 - 1.0
    val priority: Int,  // 1=high, 2=medium, 3=low
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        val hr = LocalTime.now().hour
        val greeting = when {
            hr < 12 -> "Good morning"
            hr < 17 -> "Good afternoon"
            else    -> "Good evening"
        }
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
        _state.update { it.copy(greeting = greeting, dateLabel = date) }

        viewModelScope.launch {
            combine(repo.stackFlow(), repo.logsFlow(30)) { stack, logs ->
                val today  = logs.find { it.date == LocalDate.now() }
                val streak = repo.computeStreak(logs, stack.size)

                // NEW: Compute suggestions
                val suggestions = computeSuggestions(stack, logs)

                _state.value = DashboardState(
                    greeting     = greeting,
                    dateLabel    = date,
                    streak       = streak.current,
                    todayMood    = today?.mood?.takeIf { it > 0 }?.toString() ?: "-",
                    todayFog     = today?.fog?.takeIf  { it > 0 }?.toString() ?: "-",
                    stackSize    = stack.size,
                    stack        = stack,
                    checkedToday = today?.checkedCompounds?.toSet() ?: emptySet(),
                    streakDays   = streak.last30,
                    recentLogs   = logs.takeLast(7),
                    isLoading    = false,
                    suggestions  = suggestions,  // NEW
                )
            }.collect()
        }
    }

    // NEW: Suggestion engine
    private fun computeSuggestions(stack: List<StackEntry>, logs: List<DayLog>): List<CompoundSuggestion> {
        if (logs.isEmpty()) return emptyList()

        val stackedCompoundNames = stack.map { it.compoundName }.toSet()
        val recentLogsWithData = logs.filter { it.mood > 0 || it.fog > 0 || it.energy > 0 }.takeLast(10)
        if (recentLogsWithData.isEmpty()) return emptyList()

        val avgMood = recentLogsWithData.map { it.mood }.average()
        val avgFog = recentLogsWithData.map { it.fog }.average()
        val avgEnergy = recentLogsWithData.map { it.energy }.average()
        val avgFocus = recentLogsWithData.map { it.focus }.average()

        val suggestions = mutableListOf<CompoundSuggestion>()

        // Suggest for low mood
        if (avgMood < 5f && "Ashwagandha" !in stackedCompoundNames) {
            val compound = CompoundData.all.find { it.name == "Ashwagandha" }
            compound?.let {
                suggestions.add(
                    CompoundSuggestion(
                        compound = it,
                        reason = "May improve your low baseline mood (avg: ${avgMood.toInt()}/10)",
                        confidence = 0.8f,
                        priority = 1,
                    )
                )
            }
        }

        // Suggest for brain fog
        if (avgFog > 5f && "Alpha-GPC" !in stackedCompoundNames) {
            val compound = CompoundData.all.find { it.name == "Alpha-GPC" }
            compound?.let {
                suggestions.add(
                    CompoundSuggestion(
                        compound = it,
                        reason = "May reduce your brain fog (avg: ${avgFog.toInt()}/10)",
                        confidence = 0.85f,
                        priority = 1,
                    )
                )
            }
        }

        // Suggest for low focus
        if (avgFocus < 5f && "L-Theanine" !in stackedCompoundNames && "Caffeine" !in stackedCompoundNames) {
            val compound = CompoundData.all.find { it.name == "L-Theanine" }
            compound?.let {
                suggestions.add(
                    CompoundSuggestion(
                        compound = it,
                        reason = "May enhance your focus capabilities (avg: ${avgFocus.toInt()}/10)",
                        confidence = 0.75f,
                        priority = 2,
                    )
                )
            }
        }

        return suggestions.sortedBy { it.priority }
    }
}
