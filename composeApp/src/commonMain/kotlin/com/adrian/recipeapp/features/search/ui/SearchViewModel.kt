package com.adrian.recipeapp.features.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.recipeapp.features.search.domain.repositories.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val searchRepository: SearchRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchUiState: StateFlow<SearchUiState> =
        _query
            .debounce(300L)
            .flatMapLatest { q ->
                if (q.isBlank()) {
                    flowOf<SearchUiState>(SearchUiState.Idle)
                } else {
                    flow<SearchUiState> {
                        emit(SearchUiState.Loading)
                        searchRepository.searchRecipes(q).fold(
                            onSuccess = { results ->
                                emit(
                                    if (results.isEmpty()) {
                                        SearchUiState.Empty
                                    } else {
                                        SearchUiState.Success(
                                            results
                                        )
                                    }
                                )
                            },
                            onFailure = { emit(SearchUiState.Error(it)) }
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchUiState.Idle
            )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
