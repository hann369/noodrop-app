package com.noodrop.app.ui.stack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noodrop.app.data.model.*
import com.noodrop.app.data.repository.NoodropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// - ViewModel -
data class StackState(
    val stack: List<StackEntry>         = emptyList(),
    val notes: String                   = "",
    val timeline: List<TimelineEntry>   = emptyList(),
    val showSheet: Boolean              = false,
    val selectedCompound: Compound      = CompoundData.all.first(),
    val doseInput: String               = CompoundData.all.first().defaultDose,
    val timingInput: Timing             = Timing.MORNING,
    val toastMsg: String?               = null,
)

@HiltViewModel
class StackViewModel @Inject constructor(
    private val repo: NoodropRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StackState())
    val state: StateFlow<StackState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repo.stackFlow(), repo.notesFlow(), repo.timelineFlow()) { stack, notes, tl ->
                _state.update { it.copy(stack = stack, notes = notes, timeline = tl) }
            }.collect()
        }
    }

    fun openSheet() = _state.update {
        val first = CompoundData.all.first()
        it.copy(showSheet = true, selectedCompound = first, doseInput = first.defaultDose, timingInput = Timing.MORNING)
    }
    fun closeSheet()                = _state.update { it.copy(showSheet = false) }
    fun selectCompound(c: Compound) = _state.update { it.copy(selectedCompound = c, doseInput = c.defaultDose) }
    fun updateDose(d: String)       = _state.update { it.copy(doseInput = d) }
    fun updateTiming(t: Timing)     = _state.update { it.copy(timingInput = t) }
    fun clearToast()                = _state.update { it.copy(toastMsg = null) }

    fun addCompound() {
        val s = _state.value
        if (s.stack.any { it.compoundName == s.selectedCompound.name }) {
            _state.update { it.copy(toastMsg = "Already in stack") }
            return
        }
        viewModelScope.launch {
            repo.addToStack(StackEntry(
                compoundName = s.selectedCompound.name,
                dose         = s.doseInput.ifBlank { s.selectedCompound.defaultDose },
                timing       = s.timingInput,
                sortOrder    = s.stack.size,
            ))
            _state.update { it.copy(showSheet = false, toastMsg = "${s.selectedCompound.name} added ?") }
        }
    }

    fun removeCompound(entry: StackEntry) = viewModelScope.launch {
        repo.removeFromStack(entry)
        _state.update { it.copy(toastMsg = "${entry.compoundName} removed") }
    }

    fun loadPreset(protocol: Protocol) = viewModelScope.launch {
        repo.loadPreset(protocol)
        _state.update { it.copy(toastMsg = "${protocol.name} loaded ?") }
    }

    fun saveNote(text: String) = viewModelScope.launch {
        repo.saveNote(text)
        _state.update { it.copy(toastMsg = "Notes saved ?") }
    }
}
