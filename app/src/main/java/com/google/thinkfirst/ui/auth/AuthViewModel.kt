package com.google.thinkfirst.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState
    
    private val _registerState = MutableLiveData<AuthState>()
    val registerState: LiveData<AuthState> = _registerState
    
    private val _passwordResetState = MutableLiveData<AuthState>()
    val passwordResetState: LiveData<AuthState> = _passwordResetState
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = AuthState.Loading
                
                // Create email from username
                val email = "$username@example.com"
                
                // Login with Firebase Authentication
                val result = auth.signInWithEmailAndPassword(email, password).await()
                
                if (result.user != null) {
                    _loginState.value = AuthState.Success
                } else {
                    _loginState.value = AuthState.Error("Authentication failed")
                }
                
            } catch (e: Exception) {
                _loginState.value = AuthState.Error(getErrorMessage(e))
            }
        }
    }
    
    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                _registerState.value = AuthState.Loading
                
                // Create email from username
                val email = "$username@example.com"
                
                // Check if username already exists in Firestore
                val usernameQuery = firestore.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .await()
                
                if (!usernameQuery.isEmpty) {
                    _registerState.value = AuthState.Error("Username already exists")
                    return@launch
                }
                
                // Create user with Firebase Authentication
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                if (result.user != null) {
                    // Save additional user data to Firestore
                    saveUserToFirestore(result.user!!.uid, username, email)
                    _registerState.value = AuthState.Success
                } else {
                    _registerState.value = AuthState.Error("Registration failed")
                }
                
            } catch (e: Exception) {
                _registerState.value = AuthState.Error(getErrorMessage(e))
            }
        }
    }
    
    private suspend fun saveUserToFirestore(uid: String, username: String, email: String) {
        try {
            val user = hashMapOf(
                "uid" to uid,
                "username" to username,
                "email" to email,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection("users").document(uid).set(user).await()
        } catch (e: Exception) {
            // Firestore save error, but user registration was successful
            // We can inform the user about this
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _passwordResetState.value = AuthState.Loading
                
                auth.sendPasswordResetEmail(email).await()
                _passwordResetState.value = AuthState.Success
                
            } catch (e: Exception) {
                _passwordResetState.value = AuthState.Error(getErrorMessage(e))
            }
        }
    }
    
    fun logout() {
        auth.signOut()
    }
    
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    private fun getErrorMessage(exception: Exception): String {
        return when (exception.message) {
            "The email address is badly formatted." -> "Invalid email address"
            "The password is invalid or the user does not have a password." -> "Invalid password"
            "There is no user record corresponding to this identifier. The user may have been deleted." -> "User not found"
            "The email address is already in use by another account." -> "Username already exists"
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> "Network connection error"
            else -> exception.message ?: "Unknown error"
        }
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
} 