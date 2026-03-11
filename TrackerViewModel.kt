package com.noodrop.app.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// - ViewModel -
data class TrackerState(
    val dateLabel: String       = "",
    val stack: List<StackEntry> = emptyList(),
    val checked: Set<String>    = emptySet(),
    val mood: Float             = 5f,
    val fog: Float              = 5f,
    val energy: Float           = 5f,
    val health: Float           = 5f,
    val focus: Float            = 5f,
    val notes: String           = "",
    val toast: String?          = null,
)

@HiltViewModel
class TrackerViewModel @Inject constructor(private val repo: NoodropRepository) : ViewModel() {

    private val _state = MutableStateFlow(TrackerState())
    val state: StateFlow<TrackerState> = _state.asStateFlow()

    init {
        _state.update { it.copy(dateLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))) }
        viewModelScope.launch {
            repo.stackFlow().collect { stack ->
                val today = repo.getTodayLog()
                _state.update { s -> s.copy(
                    stack   = stack,
                    checked = today?.checkedCompounds?.toSet() ?: emptySet(),
                    mood    = today?.mood?.takeIf   { it > 0 }?.toFloat() ?: 5f,
                    fog     = today?.fog?.takeIf    { it > 0 }?.toFloat() ?: 5f,
                    energy  = today?.energy?.takeIf { it > 0 }?.toFloat() ?: 5f,
                    health  = today?.health?.takeIf { it > 0 }?.toFloat() ?: 5f,
                    focus   = today?.focus?.takeIf  { it > 0 }?.toFloat() ?: 5f,
                    notes   = today?.notes ?: "",
                )}
            }
        }
    }

    fun toggleCheck(name: String) {
        _state.update {
            val cur = it.checked.toMutableSet()
            if (name in cur) cur.remove(name) else cur.add(name)
            it.copy(checked = cur)
        }
        // Auto-save after toggle
        saveCheckIn()
    }

    fun setMood(v: Float)    = _state.update { it.copy(mood = v) }
    fun setFog(v: Float)     = _state.update { it.copy(fog = v) }
    fun setEnergy(v: Float)  = _state.update { it.copy(energy = v) }
    fun setHealth(v: Float)  = _state.update { it.copy(health = v) }
    fun setFocus(v: Float)   = _state.update { it.copy(focus = v) }
    fun setNotes(n: String)  = _state.update { it.copy(notes = n) }
    fun clearToast()         = _state.update { it.copy(toast = null) }

    fun saveCheckIn() {
        val s = _state.value
        viewModelScope.launch {
            val existing = repo.getTodayLog() ?: DayLog(date = LocalDate.now())
            repo.upsertLog(existing.copy(checkedCompounds = s.checked.toList(), stackSize = s.stack.size))
            _state.update { it.copy(toast = "Check-in saved ?") }
        }
    }

    fun logDay() {
        val s = _state.value
        viewModelScope.launch {
            try {
                repo.upsertLog(DayLog(
                    date             = LocalDate.now(),
                    mood             = s.mood.toInt(),
                    fog              = s.fog.toInt(),
                    energy           = s.energy.toInt(),
                    health           = s.health.toInt(),
                    focus            = s.focus.toInt(),
                    notes            = s.notes,
                    checkedCompounds = s.checked.toList(),
                    stackSize        = s.stack.size,
                ))
                _state.update { it.copy(toast = "Day logged ✓") }
            } catch (e: Exception) {
                _state.update { it.copy(toast = "Error logging day: ${e.message}") }
            }
        }
    }
}
