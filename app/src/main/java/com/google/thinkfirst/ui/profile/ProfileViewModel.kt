package com.google.thinkfirst.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message
    
    init {
        loadUserData()
    }
    
    fun loadUserData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Get user data from Firestore
                    val userDoc = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()
                    
                    if (userDoc.exists()) {
                        val username = userDoc.getString("username") ?: "Unknown"
                        
                        _userData.value = UserData(
                            username = username,
                            uid = currentUser.uid
                        )
                    } else {
                        // Fallback to Firebase Auth data
                        _userData.value = UserData(
                            username = currentUser.displayName ?: "Unknown",
                            uid = currentUser.uid
                        )
                    }
                } else {
                    _message.value = "User not logged in"
                }
                
            } catch (e: Exception) {
                _message.value = "Error loading user data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Delete from Firestore first
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .delete()
                        .await()
                    
                    // Delete from Firebase Auth
                    currentUser.delete().await()
                    
                    _message.value = "Account deleted successfully"
                } else {
                    _message.value = "User not logged in"
                }
                
            } catch (e: Exception) {
                _message.value = "Error deleting account: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout() {
        auth.signOut()
        _message.value = "Logged out successfully"
    }
    
    fun clearMessage() {
        _message.value = null
    }
}

data class UserData(
    val username: String,
    val uid: String
) 