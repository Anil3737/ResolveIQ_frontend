package com.simats.resolveiq_frontend.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.resolveiq_frontend.data.models.User
import com.simats.resolveiq_frontend.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for Login screen
 * Manages authentication state and API calls
 */
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState
    
    /**
     * Perform login with email and password
     */
    fun login(email: String, password: String) {
        // Validate inputs
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password are required")
            return
        }
        
        // Show loading
        _loginState.value = LoginState.Loading
        
        // Call API
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            
            if (result.isSuccess) {
                val loginResponse = result.getOrNull()
                if (loginResponse != null) {
                    _loginState.value = LoginState.Success(loginResponse.user)
                } else {
                    _loginState.value = LoginState.Error("Login succeeded but data was missing")
                }
            } else {
                val error = result.exceptionOrNull()
                val message = error?.message ?: "Login failed. Please try again."
                _loginState.value = LoginState.Error(message)
            }
        }
    }
    
    /**
     * Reset login state to idle
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Sealed class representing login screen states
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}
