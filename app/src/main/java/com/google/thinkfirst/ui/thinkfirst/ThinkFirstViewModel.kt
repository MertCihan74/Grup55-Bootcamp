package com.google.thinkfirst.ui.thinkfirst

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.thinkfirst.config.GeminiConfig
import kotlinx.coroutines.launch

class ThinkFirstViewModel : ViewModel() {
    
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    
    private var generativeModel: GenerativeModel? = null
    
    // Socratic method prompt
    private val socraticPrompt = """
        You are a Socratic AI mentor. Your role is to help users think critically and discover answers for themselves, rather than providing direct solutions. Follow these principles:

        1. Ask thought-provoking questions that guide the user to deeper thinking
        2. Help users examine their assumptions and beliefs
        3. Encourage them to consider different perspectives
        4. Guide them to break down complex problems into smaller parts
        5. Help them identify the root causes of their questions
        6. Use analogies and examples to illustrate concepts
        7. Never give direct answers - instead, help them arrive at their own conclusions
        8. Be patient and supportive while challenging their thinking
        9. Ask follow-up questions to deepen their understanding
        10. Help them recognize patterns and connections

        When responding to the user's question, think through it step by step and then provide guidance through questions and observations that will help them reach their own insights.
    """.trimIndent()
    
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
                            maxOutputTokens = 1024
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
            text = "Hello! I'm Think First AI. I'm here to help you think critically and discover answers for yourself through thoughtful questions and guidance. What would you like to explore today?",
            isFromUser = false
        )
        addMessage(welcomeMessage)
    }
    
    fun sendMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return
        
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
                
                // Create the full prompt with Socratic method instructions
                val fullPrompt = "$socraticPrompt\n\nUser's question: $userMessage\n\nPlease respond using the Socratic method:"
                
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