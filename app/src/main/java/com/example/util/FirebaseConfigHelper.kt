package com.example.util

import android.content.Context
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FirebaseConfigHelper {
    private const val TAG = "FirebaseConfig"

    private val _isFirebaseReady = MutableStateFlow(false)
    val isFirebaseReady: StateFlow<Boolean> = _isFirebaseReady.asStateFlow()

    private val _cloudSyncEnabled = MutableStateFlow(false)
    val cloudSyncEnabled: StateFlow<Boolean> = _cloudSyncEnabled.asStateFlow()

    fun initialize(context: Context) {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                _isFirebaseReady.value = true
                YouPlayerLogger.i(TAG, "Firebase initialized with app: ${apps[0].name}")
            } else {
                YouPlayerLogger.i(TAG, "Running in zero-config offline mode; local Room database active")
            }
        } catch (e: Exception) {
            YouPlayerLogger.w(TAG, "Firebase initialization info: ${e.message}. Using high-speed local Room persistence.")
        }
    }

    fun setCloudSync(enabled: Boolean) {
        _cloudSyncEnabled.value = enabled
        YouPlayerLogger.i(TAG, "Cloud sync state toggled: $enabled")
    }
}
