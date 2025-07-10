package com.google.thinkfirst.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.thinkfirst.R
import com.google.thinkfirst.databinding.ActivityRegisterBinding
import com.google.thinkfirst.ui.main.MainActivity

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        // Kayıt ol butonu
        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
            val termsAccepted = binding.termsCheckBox.isChecked
            
            if (validateInputs(username, password, confirmPassword, termsAccepted)) {
                viewModel.register(username, password)
            }
        }
        
        // Giriş yap butonu
        binding.loginButton.setOnClickListener {
            finish()
        }
    }
    
    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                }
                is AuthState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, R.string.success_registration, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                }
            }
        }
    }
    
    private fun validateInputs(
        username: String,
        password: String,
        confirmPassword: String,
        termsAccepted: Boolean
    ): Boolean {
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
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.error = getString(R.string.error_field_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.error_passwords_not_match)
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }
        
        if (!termsAccepted) {
            Toast.makeText(this, R.string.error_terms_not_accepted, Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        return isValid
    }
} 