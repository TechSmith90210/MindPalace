package com.mindpalace.app.presentation.screens.auth.signUp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindpalace.app.domain.usecase.auth.GoogleSignInUseCase
import com.mindpalace.app.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val GoogleSignInCase: GoogleSignInUseCase
) : ViewModel() {
    private val _signupState = MutableStateFlow<SignupState>(SignupState.Idle)
    val signupState: StateFlow<SignupState> = _signupState

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            val result = signUpUseCase.execute(email, password)

            result.fold(
                onSuccess = {
                    _signupState.value = SignupState.Success
                },
                onFailure = { error ->
                    // Check if the error is from Supabase and provide appropriate messages
                    val errorMessage = when (error.message) {
                        "email_taken" -> "This email is already taken"
                        "invalid_email" -> "Invalid email address"
                        "password_too_short" -> "Password must be at least 6 characters"
                        else -> error.message ?: "An unknown error occurred"
                    }
                    _signupState.value = SignupState.Error(message = errorMessage)
                }
            )
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _signupState.value = SignupState.Loading

            val result = GoogleSignInCase.invoke(context)

            result.fold(
                onSuccess = {
                    _signupState.value = SignupState.Success
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Google Sign-In failed. Please try again."
                    _signupState.value = SignupState.Error(message = errorMessage)
                }
            )
        }
    }

}