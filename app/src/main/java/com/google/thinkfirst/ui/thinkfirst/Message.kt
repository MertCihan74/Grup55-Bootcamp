package com.google.thinkfirst.ui.thinkfirst

data class Message(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) 