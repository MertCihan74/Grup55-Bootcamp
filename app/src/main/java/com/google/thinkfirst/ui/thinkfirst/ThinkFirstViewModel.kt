package com.google.thinkfirst.ui.thinkfirst

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.thinkfirst.config.GeminiConfig
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ThinkFirstViewModel : ViewModel() {
    
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private val _dailyQuestionCount = MutableLiveData<Int>()
    val dailyQuestionCount: LiveData<Int> = _dailyQuestionCount
    
    private var generativeModel: GenerativeModel? = null
    private lateinit var sharedPreferences: SharedPreferences
    
    // Socratic method prompt for children and young people
    private val socraticPrompt = """
        You are a friendly AI mentor for children and young people. Help them think and discover answers themselves using the Socratic method:

        1. Ask simple, fun questions that make them think
        2. Help break big problems into smaller parts
        3. Use examples from daily life (school, friends, family, games)
        4. Ask "What do you think?" and "Why do you think that?"
        5. Help see different sides of a situation
        6. Use simple analogies they can relate to
        7. Be positive and supportive - no wrong answers when thinking
        8. Guide step by step, like solving a puzzle
        9. Help connect ideas to find their own answer

        Be encouraging, patient, and use simple language. Make it fun!
    """.trimIndent()
    
    init {
        initializeGemini()
        addWelcomeMessage()
    }
    
    fun initializeSharedPreferences(context: Context) {
        sharedPreferences = context.getSharedPreferences("ThinkFirstPrefs", Context.MODE_PRIVATE)
        loadDailyQuestionCount()
    }
    
    private fun loadDailyQuestionCount() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastQuestionDate = sharedPreferences.getString("last_question_date", "")
        val savedCount = sharedPreferences.getInt("consecutive_days_count", 0)
        
        if (lastQuestionDate != today) {
            // No questions asked today yet
            _dailyQuestionCount.value = savedCount
        } else {
            // Already asked a question today
            _dailyQuestionCount.value = savedCount
        }
    }
    
    private fun incrementDailyQuestionCount() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastQuestionDate = sharedPreferences.getString("last_question_date", "")
        val currentCount = _dailyQuestionCount.value ?: 0
        
        if (lastQuestionDate != today) {
            // New day, increment counter
            val newCount = currentCount + 1
            _dailyQuestionCount.value = newCount
            
            // Save to SharedPreferences
            sharedPreferences.edit()
                .putString("last_question_date", today)
                .putInt("consecutive_days_count", newCount)
                .apply()
        }
        // If asking another question on the same day, counter doesn't increase
    }
    
    private fun checkAndResetCount() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastQuestionDate = sharedPreferences.getString("last_question_date", "")
        
        if (lastQuestionDate?.isNotEmpty() == true && lastQuestionDate != today) {
            // Asked a question yesterday but not today, reset counter
            val yesterday = getYesterdayDate()
            if (lastQuestionDate == yesterday) {
                // Asked a question yesterday but not today
                _dailyQuestionCount.value = 0
                sharedPreferences.edit()
                    .putInt("consecutive_days_count", 0)
                    .apply()
            }
        }
    }
    
    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
    
    private fun initializeGemini() {
        try {
            // Check if API key is configured
            if (GeminiConfig.API_KEY == "YOUR_GEMINI_API_KEY_HERE") {
                _error.value = "Please configure your Gemini API key in GeminiConfig.kt"
                return
            }
            
            // Try different model names
            val modelNames = listOf("gemini-2.5-flash", "gemini-pro", "gemini-1.5-pro", "gemini-1.0-pro")
            var modelInitialized = false
            
            for (modelName in modelNames) {
                try {
                    Log.d("GeminiInit", "Trying model: $modelName")
                    generativeModel = GenerativeModel(
                        modelName = modelName,
                        apiKey = GeminiConfig.API_KEY,
                        generationConfig = generationConfig {
                            temperature = 0.7f
                            topK = 40
                            topP = 0.95f
                            maxOutputTokens = 2048
                        }
                    )
                    Log.d("GeminiInit", "Successfully initialized model: $modelName")
                    modelInitialized = true
                    break
                } catch (e: Exception) {
                    Log.e("GeminiInit", "Failed to initialize model $modelName: ${e.message}")
                    continue
                }
            }
            
            if (!modelInitialized) {
                _error.value = "Failed to initialize any Gemini model. Please check your API key."
            }
            
        } catch (e: Exception) {
            Log.e("GeminiInit", "General initialization error: ${e.message}")
            _error.value = "Failed to initialize Gemini: ${e.message}"
        }
    }
    
    private fun addWelcomeMessage() {
        val welcomeMessage = Message(
            text = "Hi there! 👋 I'm Think First AI, your friendly thinking buddy! I'm here to help you figure things out by asking fun questions and helping you think. Instead of giving you answers, I'll help you find them yourself! What would you like to think about today?",
            isFromUser = false
        )
        addMessage(welcomeMessage)
    }
    
    fun sendMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return
        
        // Increment daily question counter (only for new days)
        incrementDailyQuestionCount()
        
        // Add user message
        val userMessage = Message(text = messageText.trim(), isFromUser = true)
        addMessage(userMessage)
        
        // Get AI response with Socratic method
        getAIResponse(messageText.trim())
    }
    
    private fun getAIResponse(userMessage: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val model = generativeModel
                if (model == null) {
                    _error.value = "Gemini is not initialized. Please check your API key."
                    _isLoading.value = false
                    return@launch
                }
                
                // Create a shorter prompt with Socratic method instructions
                val fullPrompt = "$socraticPrompt\n\nUser: $userMessage\n\nRespond using the Socratic method:"
                
                Log.d("GeminiAPI", "Sending Socratic prompt for message: $userMessage")
                val response = model.generateContent(fullPrompt)
                val responseText = response.text ?: "I'm sorry, I couldn't generate a response."
                
                Log.d("GeminiAPI", "Received Socratic response: $responseText")
                val aiMessage = Message(text = responseText, isFromUser = false)
                addMessage(aiMessage)
                
            } catch (e: Exception) {
                Log.e("GeminiAPI", "Error getting response", e)
                _error.value = "Error getting response: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun addMessage(message: Message) {
        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _messages.value = currentMessages
    }
    
    fun clearError() {
        _error.value = null
    }
} 