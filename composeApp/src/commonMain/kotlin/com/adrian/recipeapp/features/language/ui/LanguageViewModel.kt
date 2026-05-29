package com.adrian.recipeapp.features.language.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adrian.recipeapp.features.language.domain.entities.AppLang
import com.adrian.recipeapp.features.language.domain.managers.AppLocaleManager
import com.adrian.recipeapp.features.language.domain.repositories.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LanguageViewModel(
    private val repository: LanguageRepository,
    private val localeManager: AppLocaleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState(isLoading = true))
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSelectedLang().collect { result ->
                val lang = result.getOrDefault(AppLang.default)
                _uiState.update {
                    it.copy(
                        currentLang = lang,
                        pendingLang = lang,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onLanguageRowTapped() {
        _uiState.update {
            it.copy(
                pendingLang = it.currentLang,
                isBottomSheetVisible = true
            )
        }
    }

    fun onLanguageSelected(lang: AppLang) {
        _uiState.update { it.copy(pendingLang = lang) }
    }

    fun onApply() {
        val pending = _uiState.value.pendingLang
        viewModelScope.launch {
            repository.saveSelectedLang(pending)
            localeManager.applyLocale(pending.code)
            _uiState.update {
                it.copy(
                    currentLang = pending,
                    isBottomSheetVisible = false
                )
            }
        }
    }

    fun onDismiss() {
        _uiState.update {
            it.copy(
                pendingLang = it.currentLang,
                isBottomSheetVisible = false
            )
        }
    }
}
