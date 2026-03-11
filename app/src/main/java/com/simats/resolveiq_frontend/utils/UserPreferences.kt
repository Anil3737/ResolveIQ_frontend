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
        // Pending submission tracking keys to prevent duplicate tickets
        private const val KEY_PENDING_IDEMPOTENCY_KEY = "pending_idempotency_key"
        private const val KEY_PENDING_TITLE = "pending_title"
        private const val KEY_PENDING_DEPARTMENT_ID = "pending_department_id"
        private const val KEY_PENDING_TIMESTAMP = "pending_timestamp"
        private const val KEY_PENDING_COMPLETED = "pending_completed"
        // Duplicate guard window: 2 minutes in milliseconds
        private const val DUPLICATE_GUARD_WINDOW_MS = 2 * 60 * 1000L
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

    // ───────────────────────────────────────────────────────────────
    // Pending Submission Tracking — prevents duplicate ticket creation
    // ───────────────────────────────────────────────────────────────

    /**
     * Saves a pending ticket submission so we can detect duplicates on retry.
     */
    fun savePendingSubmission(idempotencyKey: String, title: String, departmentId: Int) {
        prefs.edit()
            .putString(KEY_PENDING_IDEMPOTENCY_KEY, idempotencyKey)
            .putString(KEY_PENDING_TITLE, title)
            .putInt(KEY_PENDING_DEPARTMENT_ID, departmentId)
            .putLong(KEY_PENDING_TIMESTAMP, System.currentTimeMillis())
            .putBoolean(KEY_PENDING_COMPLETED, false)
            .apply()
    }

    /**
     * Returns the idempotency key of the pending submission, or null if none.
     */
    fun getPendingIdempotencyKey(): String? {
        return prefs.getString(KEY_PENDING_IDEMPOTENCY_KEY, null)
    }

    /**
     * Returns the title of the pending submission, or null if none.
     */
    fun getPendingTitle(): String? {
        return prefs.getString(KEY_PENDING_TITLE, null)
    }

    /**
     * Returns the department ID of the pending submission, or -1.
     */
    fun getPendingDepartmentId(): Int {
        return prefs.getInt(KEY_PENDING_DEPARTMENT_ID, -1)
    }

    /**
     * Marks the pending submission as completed (ticket was created).
     */
    fun markSubmissionComplete() {
        prefs.edit().putBoolean(KEY_PENDING_COMPLETED, true).apply()
    }

    /**
     * Checks if the pending submission was already completed.
     */
    fun isSubmissionCompleted(): Boolean {
        return prefs.getBoolean(KEY_PENDING_COMPLETED, false)
    }

    /**
     * Clears all pending submission data.
     */
    fun clearPendingSubmission() {
        prefs.edit()
            .remove(KEY_PENDING_IDEMPOTENCY_KEY)
            .remove(KEY_PENDING_TITLE)
            .remove(KEY_PENDING_DEPARTMENT_ID)
            .remove(KEY_PENDING_TIMESTAMP)
            .remove(KEY_PENDING_COMPLETED)
            .apply()
    }

    /**
     * Checks if a submission with the same title and department was made
     * within the duplicate guard window (2 minutes). Returns true if
     * a duplicate submission is detected (should block re-submission).
     */
    fun isDuplicateSubmission(title: String, departmentId: Int): Boolean {
        val pendingTitle = getPendingTitle() ?: return false
        val pendingDeptId = getPendingDepartmentId()
        val pendingTimestamp = prefs.getLong(KEY_PENDING_TIMESTAMP, 0L)
        val elapsed = System.currentTimeMillis() - pendingTimestamp

        // Check if same title + department and within the guard window
        return pendingTitle == title
                && pendingDeptId == departmentId
                && elapsed < DUPLICATE_GUARD_WINDOW_MS
    }
}

