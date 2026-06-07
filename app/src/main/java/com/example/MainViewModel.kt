package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository = Repository()

    private val _allChannels = MutableStateFlow<List<ChannelStream>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> = _allChannels
        .combine(MutableStateFlow(Unit)) { channels, _ ->
            channels.flatMap { it.categories }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val displayedChannels = combine(_allChannels, _searchQuery, _selectedCategory) { channels, query, category ->
        channels.filter { channel ->
            val matchesQuery = if (query.isBlank()) true else channel.name.contains(query, ignoreCase = true)
            val matchesCategory = if (category == null) true else channel.categories.contains(category)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getChannelsAndStreams()
            _allChannels.value = data
            _isLoading.value = false
        }
    }

    fun onSearchQueryComplete(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }
}
