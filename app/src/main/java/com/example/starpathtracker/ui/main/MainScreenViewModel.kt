package com.example.starpathtracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starpathtracker.data.StarPathRepository
import com.example.starpathtracker.data.model.StarPathDuty
import com.example.starpathtracker.data.model.StarPathEntry
import com.example.starpathtracker.data.model.StarPathList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DutyTab(val label: String) {
    PATH("Path"),
    WEEKLY("Weekly")
}

enum class SortOrder(val label: String) {
    DEFAULT("Default"),
    REWARD_HIGH_TO_LOW("Tokens (High-Low)"),
    TITLE("Name (A-Z)")
}

private data class FilterState(
    val selectedTab: DutyTab = DutyTab.PATH,
    val hideCompleted: Boolean = false,
    val query: String = "",
    val sort: SortOrder = SortOrder.DEFAULT
)

private data class UiOperationState(
    val isLoading: Boolean = true,
    val isScraping: Boolean = false,
    val errorMessage: String? = null,
    val isDropdownExpanded: Boolean = false,
    val isResetConfirmDialogOpen: Boolean = false
)

data class MainUiState(
    val starPathList: StarPathList? = null,
    val availableStarPaths: List<StarPathEntry> = emptyList(),
    val filteredDuties: List<StarPathDuty> = emptyList(),
    val selectedTab: DutyTab = DutyTab.PATH,
    val hideCompleted: Boolean = false,
    val pathCount: Int = 0,
    val weeklyCount: Int = 0,
    val isLoading: Boolean = true,
    val isScraping: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val isDropdownExpanded: Boolean = false,
    val isResetConfirmDialogOpen: Boolean = false
)

class MainScreenViewModel(private val repository: StarPathRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    private val _uiOpState = MutableStateFlow(UiOperationState())

    val uiState: StateFlow<MainUiState> = combine(
        repository.currentStarPath,
        repository.starPathEntries,
        _filterState,
        _uiOpState
    ) { starPath: StarPathList?, entries: List<StarPathEntry>, filters: FilterState, op: UiOperationState ->
        val duties = starPath?.duties ?: emptyList()
        val pathDuties = duties.filter { !it.isWeeklyDuty }
        val weeklyDuties = duties.filter { it.isWeeklyDuty }

        val baseTabDuties = if (filters.selectedTab == DutyTab.PATH) pathDuties else weeklyDuties

        val filtered = baseTabDuties.filter { duty ->
            val matchesQuery = filters.query.isBlank() ||
                    duty.title.contains(filters.query, ignoreCase = true) ||
                    duty.howToComplete.contains(filters.query, ignoreCase = true) ||
                    duty.requirement.contains(filters.query, ignoreCase = true) ||
                    duty.section.contains(filters.query, ignoreCase = true)

            val matchesHideCompleted = if (filters.hideCompleted) !duty.isCompleted else true

            matchesQuery && matchesHideCompleted
        }.let { list ->
            when (filters.sort) {
                SortOrder.DEFAULT -> list
                SortOrder.REWARD_HIGH_TO_LOW -> list.sortedByDescending { it.tokenReward }
                SortOrder.TITLE -> list.sortedBy { it.title }
            }
        }

        MainUiState(
            starPathList = starPath,
            availableStarPaths = entries,
            filteredDuties = filtered,
            selectedTab = filters.selectedTab,
            hideCompleted = filters.hideCompleted,
            pathCount = pathDuties.size,
            weeklyCount = weeklyDuties.size,
            isLoading = op.isLoading,
            isScraping = op.isScraping,
            errorMessage = op.errorMessage,
            searchQuery = filters.query,
            sortOrder = filters.sort,
            isDropdownExpanded = op.isDropdownExpanded,
            isResetConfirmDialogOpen = op.isResetConfirmDialogOpen
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState(availableStarPaths = StarPathRepository.DEFAULT_ENTRIES)
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiOpState.value = _uiOpState.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.initialize()
            } catch (e: Exception) {
                _uiOpState.value = _uiOpState.value.copy(errorMessage = e.message ?: "Failed to load duties")
            } finally {
                _uiOpState.value = _uiOpState.value.copy(isLoading = false)
            }
        }
    }

    fun selectTab(tab: DutyTab) {
        _filterState.value = _filterState.value.copy(selectedTab = tab)
    }

    fun toggleHideCompleted() {
        _filterState.value = _filterState.value.copy(hideCompleted = !_filterState.value.hideCompleted)
    }

    fun onSearchQueryChanged(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun onSortOrderChanged(sort: SortOrder) {
        _filterState.value = _filterState.value.copy(sort = sort)
    }

    fun setDropdownExpanded(expanded: Boolean) {
        _uiOpState.value = _uiOpState.value.copy(isDropdownExpanded = expanded)
    }

    fun selectStarPath(url: String) {
        setDropdownExpanded(false)
        loadUrl(url, forceScrape = false)
    }

    fun toggleDuty(dutyId: String) {
        viewModelScope.launch {
            repository.toggleDutyCompleted(dutyId)
        }
    }

    fun openResetDialog() {
        _uiOpState.value = _uiOpState.value.copy(isResetConfirmDialogOpen = true)
    }

    fun closeResetDialog() {
        _uiOpState.value = _uiOpState.value.copy(isResetConfirmDialogOpen = false)
    }

    fun resetAllDuties() {
        viewModelScope.launch {
            repository.resetAllChecked()
            _uiOpState.value = _uiOpState.value.copy(isResetConfirmDialogOpen = false)
        }
    }

    fun markAllDuties() {
        viewModelScope.launch {
            repository.markAllChecked()
        }
    }

    fun loadUrl(url: String, forceScrape: Boolean = false) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _uiOpState.value = _uiOpState.value.copy(isScraping = true, errorMessage = null)
            try {
                val result = repository.loadStarPath(url.trim(), forceScrape = forceScrape)
                if (result.isFailure) {
                    _uiOpState.value = _uiOpState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to scrape Star Path data"
                    )
                }
            } catch (e: Exception) {
                _uiOpState.value = _uiOpState.value.copy(errorMessage = e.message ?: "Unexpected error")
            } finally {
                _uiOpState.value = _uiOpState.value.copy(isScraping = false)
            }
        }
    }

    fun refreshFromWeb() {
        val currentUrl = repository.getActiveUrl()
        loadUrl(currentUrl, forceScrape = true)
    }

    fun clearError() {
        _uiOpState.value = _uiOpState.value.copy(errorMessage = null)
    }
}
