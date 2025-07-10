package com.google.thinkfirst.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.thinkfirst.R
import com.google.thinkfirst.databinding.ActivityMainBinding
import com.google.thinkfirst.ui.auth.LoginActivity
import com.google.thinkfirst.ui.profile.ProfileActivity
import com.google.thinkfirst.ui.quickask.QuickAskActivity
import com.google.thinkfirst.ui.thinkfirst.ThinkFirstActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            // User is not logged in, redirect to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            // User is logged in, show main screen
            setupMainScreen()
        }
    }
    
    private fun setupMainScreen() {
        // Setup click listeners for cards
        setupCardClickListeners()
        
        // Setup profile button
        setupProfileButton()
    }
    
    private fun setupCardClickListeners() {
        // Quick Ask Card
        binding.quickAskCard.setOnClickListener {
            startActivity(Intent(this, QuickAskActivity::class.java))
        }
        
        // Think First Card
        binding.thinkFirstCard.setOnClickListener {
            startActivity(Intent(this, ThinkFirstActivity::class.java))
        }
        
        // Coming Soon Card
        binding.comingSoonCard.setOnClickListener {
            Toast.makeText(this, "This feature is under development!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupProfileButton() {
        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
} 