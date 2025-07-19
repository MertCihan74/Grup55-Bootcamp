package com.google.thinkfirst.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.thinkfirst.databinding.ActivityProfileBinding
import com.google.thinkfirst.ui.auth.LoginActivity

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewModel()
        setupButtons()
        setupBackPress()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Profile"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        
        // Observe user data
        viewModel.userData.observe(this) { userData ->
            binding.usernameText.text = userData.username
        }
        
        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.editProfileButton.isEnabled = !isLoading
            binding.deleteAccountButton.isEnabled = !isLoading
        }
        
        // Observe messages
        viewModel.message.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearMessage()
                
                // If account deleted or logged out, go to login screen
                if (it.contains("deleted") || it.contains("logged out")) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
    
    private fun setupButtons() {
        // Edit Profile Button
        binding.editProfileButton.setOnClickListener {
            val currentUsername = binding.usernameText.text.toString()
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("current_username", currentUsername)
            startActivity(intent)
        }
        
        // Delete Account Button
        binding.deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }
    }
    
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        // Reload user data in case it was updated in EditProfileActivity
        viewModel.loadUserData()
    }
} 