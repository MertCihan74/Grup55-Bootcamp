package com.google.thinkfirst.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.thinkfirst.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewModel()
        setupSaveButton()
        setupBackPress()
        
        // Set current username
        val currentUsername = intent.getStringExtra("current_username") ?: "Unknown"
        binding.currentUsernameText.text = currentUsername
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profile"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.saveChangesButton.isEnabled = !isLoading
        }
        
        // Observe messages
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
            }
        }
        
        // Observe success
        viewModel.success.observe(this) { success ->
            if (success) {
                // Clear form fields on success
                binding.newUsernameEditText.text?.clear()
                binding.currentPasswordEditText.text?.clear()
                binding.newPasswordEditText.text?.clear()
                binding.confirmPasswordEditText.text?.clear()
                viewModel.clearSuccess()
            }
        }
    }
    
    private fun setupSaveButton() {
        binding.saveChangesButton.setOnClickListener {
            val newUsername = binding.newUsernameEditText.text.toString().trim()
            val currentPassword = binding.currentPasswordEditText.text.toString()
            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            
            // Check if any field is filled
            if (newUsername.isEmpty() && currentPassword.isEmpty() && newPassword.isEmpty() && confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill at least one field to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Update username if provided
            if (newUsername.isNotEmpty()) {
                viewModel.updateUsername(newUsername)
            }
            
            // Update password if all password fields are provided
            if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
                viewModel.updatePassword(currentPassword, newPassword, confirmPassword)
            } else if (currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
                Toast.makeText(this, "Please fill all password fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
} 