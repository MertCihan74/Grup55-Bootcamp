package com.google.thinkfirst.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditProfileViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    
    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> = _success
    
    fun updateUsername(newUsername: String) {
        if (newUsername.trim().isEmpty()) {
            _message.value = "Username cannot be empty"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Check if username already exists
                    val usernameQuery = firestore.collection("users")
                        .whereEqualTo("username", newUsername.trim())
                        .get()
                        .await()
                    
                    if (!usernameQuery.isEmpty) {
                        _message.value = "Username already exists"
                        return@launch
                    }
                    
                    // Update username in Firestore
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .update("username", newUsername.trim())
                        .await()
                    
                    _message.value = "Username updated successfully"
                    _success.value = true
                } else {
                    _message.value = "User not logged in"
                }
                
            } catch (e: Exception) {
                _message.value = "Error updating username: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updatePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isEmpty()) {
            _message.value = "Current password is required"
            return
        }
        
        if (newPassword.isEmpty()) {
            _message.value = "New password is required"
            return
        }
        
        if (newPassword.length < 6) {
            _message.value = "New password must be at least 6 characters"
            return
        }
        
        if (newPassword != confirmPassword) {
            _message.value = "New passwords do not match"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Re-authenticate user before password change
                    val email = currentUser.email
                    if (email != null) {
                        // Re-authenticate with current password
                        val credential = com.google.firebase.auth.EmailAuthProvider
                            .getCredential(email, currentPassword)
                        
                        currentUser.reauthenticate(credential).await()
                        
                        // Update password
                        currentUser.updatePassword(newPassword).await()
                        
                        _message.value = "Password updated successfully"
                        _success.value = true
                    } else {
                        _message.value = "Email not found"
                    }
                } else {
                    _message.value = "User not logged in"
                }
                
            } catch (e: Exception) {
                _message.value = "Error updating password: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }
    
    fun clearSuccess() {
        _success.value = false
    }
} 