package com.simats.resolveiq_frontend

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class ResolveIQApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val userPrefs = com.simats.resolveiq_frontend.utils.UserPreferences(this)
        if (userPrefs.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
