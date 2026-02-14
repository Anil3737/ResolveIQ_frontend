package com.simats.resolveiq_frontend.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.simats.resolveiq_frontend.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property to get DataStore instance
private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    
    companion object {
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_ACTIVE_KEY = stringPreferencesKey("user_is_active")
        
        @Volatile
        private var instance: UserPreferences? = null
        
        fun getInstance(context: Context): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Save user information to DataStore
     */
    suspend fun saveUser(user: User) {
        context.userDataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.user_id
            preferences[USER_NAME_KEY] = user.full_name
            preferences[USER_EMAIL_KEY] = user.email
            preferences[USER_ROLE_KEY] = user.role
            user.phone?.let { preferences[USER_PHONE_KEY] = it }
            preferences[USER_ACTIVE_KEY] = user.is_active.toString()
        }
    }
    
    /**
     * Get user information from DataStore
     */
    suspend fun getUser(): User? {
        val preferences = context.userDataStore.data.first()
        val userId = preferences[USER_ID_KEY] ?: return null
        
        return User(
            user_id = userId,
            full_name = preferences[USER_NAME_KEY] ?: "",
            email = preferences[USER_EMAIL_KEY] ?: "",
            phone = preferences[USER_PHONE_KEY],
            role = preferences[USER_ROLE_KEY] ?: "",
            is_active = preferences[USER_ACTIVE_KEY]?.toBoolean() ?: true
        )
    }
    
    /**
     * Get user as Flow (for reactive UI updates)
     */
    fun getUserFlow(): Flow<User?> {
        return context.userDataStore.data.map { preferences ->
            val userId = preferences[USER_ID_KEY] ?: return@map null
            User(
                user_id = userId,
                full_name = preferences[USER_NAME_KEY] ?: "",
                email = preferences[USER_EMAIL_KEY] ?: "",
                phone = preferences[USER_PHONE_KEY],
                role = preferences[USER_ROLE_KEY] ?: "",
                is_active = preferences[USER_ACTIVE_KEY]?.toBoolean() ?: true
            )
        }    }
    
    /**
     * Get user role
     */
    suspend fun getUserRole(): String? {
        return context.userDataStore.data.first()[USER_ROLE_KEY]
    }
    
    /**
     * Check if user is admin
     */
    suspend fun isAdmin(): Boolean {
        return getUserRole() == "ADMIN"
    }
    
    /**
     * Clear user data (logout)
     */
    suspend fun clearUser() {
        context.userDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
