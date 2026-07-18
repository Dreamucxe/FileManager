package com.kiro.filemanager.presentation.recyclebin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiro.filemanager.domain.model.TrashItem
import com.kiro.filemanager.domain.repository.RecycleBinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val recycleBinRepository: RecycleBinRepository,
) : ViewModel() {

    val items: StateFlow<List<TrashItem>> = recycleBinRepository.observeTrash().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun restore(ids: List<Long>) = viewModelScope.launch { recycleBinRepository.restore(ids) }
    fun deletePermanently(ids: List<Long>) = viewModelScope.launch { recycleBinRepository.deletePermanently(ids) }
    fun emptyBin() = viewModelScope.launch { recycleBinRepository.emptyBin() }
}
