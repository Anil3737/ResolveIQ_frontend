package com.simats.resolveiq_frontend.utils

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("resolveiq_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_LOCATION = "user_location"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts_json"
    }

    fun setRememberMe(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_ME, enabled).apply()
    }

    fun getRememberMe(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun saveAccount(email: String, password: String) {
        val accounts = getSavedAccounts().toMutableMap()
        accounts[email] = password
        val json = com.google.gson.Gson().toJson(accounts)
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, json).apply()
    }

    fun getSavedAccounts(): Map<String, String> {
        val json = prefs.getString(KEY_SAVED_ACCOUNTS, null) ?: return emptyMap()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
            com.google.gson.Gson().fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun removeAccount(email: String) {
        val accounts = getSavedAccounts().toMutableMap()
        accounts.remove(email)
        val json = com.google.gson.Gson().toJson(accounts)
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, json).apply()
    }

    fun clearAllSavedAccounts() {
        prefs.edit().remove(KEY_SAVED_ACCOUNTS).apply()
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUserName(name: String?) {
        prefs.edit().putString(KEY_USER_NAME, name ?: "User").apply()
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun saveUserRole(role: String?) {
        if (!role.isNullOrBlank()) {
            prefs.edit().putString(KEY_USER_ROLE, role).apply()
        }
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_USER_ROLE, null)
    }
    
    fun saveUserId(id: Int) {
        prefs.edit().putInt(KEY_USER_ID, id).apply()
    }
    
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun saveUserEmail(email: String?) {
        prefs.edit().putString(KEY_USER_EMAIL, email ?: "").apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun saveUserLocation(location: String?) {
        prefs.edit().putString(KEY_USER_LOCATION, location).apply()
    }

    fun getUserLocation(): String? {
        return prefs.getString(KEY_USER_LOCATION, null)
    }

    fun saveUserPhone(phone: String?) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun getUserPhone(): String? {
        return prefs.getString(KEY_USER_PHONE, null)
    }

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
