package com.google.thinkfirst.ui.quickask

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.thinkfirst.config.GeminiConfig
import kotlinx.coroutines.launch

class QuickAskViewModel : ViewModel() {
    
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var generativeModel: GenerativeModel? = null
    
    init {
        initializeGemini()
        addWelcomeMessage()
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
            text = "Hello! I'm Gemini. How can I help you today?",
            isFromUser = false
        )
        addMessage(welcomeMessage)
    }
    
    fun sendMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return
        
        // Add user message
        val userMessage = Message(text = messageText.trim(), isFromUser = true)
        addMessage(userMessage)
        
        // Get AI response
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
                
                // Create a simple prompt to avoid token limits
                val prompt = "Answer this question briefly and clearly: $userMessage"
                
                Log.d("GeminiAPI", "Sending message: $userMessage")
                val response = model.generateContent(prompt)
                val responseText = response.text ?: "I'm sorry, I couldn't generate a response."
                
                Log.d("GeminiAPI", "Received response: $responseText")
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