package com.adrian.recipeapp.features.profile.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState(email = "test@gmail.com"))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}
