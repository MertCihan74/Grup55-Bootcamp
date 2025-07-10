package com.google.thinkfirst.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.thinkfirst.R
import com.google.thinkfirst.databinding.ActivityLoginBinding
import com.google.thinkfirst.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        // Giriş butonu
        binding.loginButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            
            if (validateInputs(username, password)) {
                viewModel.login(username, password)
            }
        }
        
        // Kayıt ol butonu
        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    
    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(this, R.string.success_login, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                }
            }
        }
        
        viewModel.passwordResetState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Password reset loading
                }
                is AuthState.Success -> {
                    Toast.makeText(this, R.string.success_password_reset, Toast.LENGTH_LONG).show()
                }
                is AuthState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    // Reset state
                }
            }
        }
    }
    
    private fun validateInputs(username: String, password: String): Boolean {
        var isValid = true
        
        if (username.isEmpty()) {
            binding.usernameLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else if (username.length < 3) {
            binding.usernameLayout.error = getString(R.string.error_invalid_username)
            isValid = false
        } else {
            binding.usernameLayout.error = null
        }
        
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_invalid_password)
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }
        
        return isValid
    }
} 