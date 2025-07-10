# Think First App

An Android application built with Kotlin that provides AI-powered chat functionality using Google's Gemini API.

## Features

- **User Authentication**: Firebase Authentication with username/password
- **Quick Ask**: AI-powered chat interface using Gemini API
- **Modern UI**: Material Design components with a clean interface

## Setup Instructions

### 1. Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add your Android app to the project
3. Download `google-services.json` and place it in the `app/` directory
4. Enable Authentication with Email/Password
5. Create a Firestore database with appropriate security rules

### 2. Gemini API Setup
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Open `app/src/main/java/com/google/thinkfirst/config/GeminiConfig.kt`
4. Replace `YOUR_GEMINI_API_KEY_HERE` with your actual API key

```kotlin
const val API_KEY = "your_actual_api_key_here"
```

### 3. Build and Run
1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run the application

## Project Structure

```
app/src/main/java/com/google/thinkfirst/
├── config/
│   └── GeminiConfig.kt          # API configuration
├── ui/
│   ├── auth/                    # Authentication screens
│   ├── main/                    # Main activity
│   └── quickask/                # Quick Ask chat feature
│       ├── QuickAskActivity.kt  # Chat interface
│       ├── QuickAskViewModel.kt # Business logic
│       ├── ChatAdapter.kt       # RecyclerView adapter
│       └── Message.kt           # Data model
```

## Quick Ask Feature

The Quick Ask feature provides a chat interface where users can:
- Send messages to Gemini AI
- Receive AI-generated responses
- View conversation history
- Navigate back to the main screen

### Chat Interface Design
- **User messages**: Appear on the right side with blue background
- **AI messages**: Appear on the left side with gray background
- **Welcome message**: "Hello! I'm Gemini. How can I help you today?"

## Dependencies

- **Firebase**: Authentication and Firestore
- **Gemini API**: AI chat functionality
- **Material Design**: UI components
- **ViewModel & LiveData**: Architecture components
- **Coroutines**: Asynchronous programming

## Troubleshooting

### Common Issues

1. **API Key Error**: Make sure you've configured the Gemini API key correctly
2. **Firebase Connection**: Ensure `google-services.json` is properly configured
3. **Build Errors**: Check that all dependencies are synced

### Getting Help

If you encounter any issues:
1. Check the Android Studio logs
2. Verify your API keys are correct
3. Ensure all dependencies are up to date

## License

This project is for educational purposes. 