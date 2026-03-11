package com.noodrop.app.ui.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.DayLog
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// - ViewModel -
data class MetricsState(
    val period: Int          = 7,
    val logs: List<DayLog>   = emptyList(),
    val avgMood: String      = "-",
    val avgFog: String       = "-",
    val avgEnergy: String    = "-",
    val avgHealth: String    = "-",
    val avgFocus: String     = "-",
    // NEW STEP 2 FIELDS
    val topCompounds: List<CompoundImpactScore> = emptyList(),
    val bestWorstAnalysis: BestWorstDaysAnalysis? = null,
    val analyticsLoading: Boolean = false,
)

@HiltViewModel
class MetricsViewModel @Inject constructor(private val repo: NoodropRepository) : ViewModel() {

    private val _state = MutableStateFlow(MetricsState())
    val state: StateFlow<MetricsState> = _state.asStateFlow()

    private var job: kotlinx.coroutines.Job? = null

    init { load(7) }

    fun setPeriod(days: Int) { load(days) }

    private fun load(days: Int) {
        job?.cancel()
        job = viewModelScope.launch {
            repo.logsFlow(days).collect { logs ->
                fun avg(sel: (DayLog) -> Int): String {
                    val v = logs.filter { sel(it) > 0 }.map { sel(it).toFloat() }
                    return if (v.isEmpty()) "-" else "%.1f".format(v.average())
                }

                // Load new Step 2 analytics
                _state.update { it.copy(analyticsLoading = true) }

                try {
                    val topCompounds = repo.computeCompoundImpacts(days)
                    val bestWorstAnalysis = repo.analyzeBestWorstDays(days)

                    _state.update { state ->
                        state.copy(
                            period    = days,
                            logs      = logs,
                            avgMood   = avg { it.mood },
                            avgFog    = avg { it.fog },
                            avgEnergy = avg { it.energy },
                            avgHealth = avg { it.health },
                            avgFocus  = avg { it.focus },
                            topCompounds = topCompounds.take(3),  // Top 3
                            bestWorstAnalysis = bestWorstAnalysis,
                            analyticsLoading = false,
                        )
                    }
                } catch (e: Exception) {
                    // Fallback if Step 2 fails
                    _state.value = MetricsState(
                        period    = days,
                        logs      = logs,
                        avgMood   = avg { it.mood },
                        avgFog    = avg { it.fog },
                        avgEnergy = avg { it.energy },
                        avgHealth = avg { it.health },
                        avgFocus  = avg { it.focus },
                    )
                }
            }
        }
    }
}
