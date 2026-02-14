package com.simats.resolveiq_frontend.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Extension property to get DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {
    
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        
        @Volatile
        private var instance: TokenManager? = null
        
        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Save JWT token to DataStore
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    /**
     * Get JWT token from DataStore (blocking)
     * Use this in interceptors where suspend functions can't be used
     */
    fun getToken(): String? {
        return runBlocking {
            context.dataStore.data.first()[TOKEN_KEY]
        }
    }
    
    /**
     * Get JWT token as Flow (non-blocking)
     * Use this in ViewModels and other coroutine contexts
     */
    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
    
    /**
     * Clear JWT token (logout)
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun hasToken(): Boolean {
        return getToken() != null
    }
}
