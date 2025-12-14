package com.teknos.oncolly.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "oncolly_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ROLE = "user_role"
    private const val KEY_TIMESTAMP = "login_timestamp"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, token: String, userId: String, role: String) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_ROLE, role)
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis())
        editor.apply()
    }

    fun getSession(context: Context): SessionData? {
        val prefs = getPrefs(context)
        val token = prefs.getString(KEY_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        val role = prefs.getString(KEY_ROLE, null)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0)

        if (token != null && userId != null && role != null) {
            // Check if expired (24 hours = 86400000 ms)
            if (System.currentTimeMillis() - timestamp < 86400000) {
                return SessionData(token, userId, role)
            } else {
                clearSession(context)
            }
        }
        return null
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}

data class SessionData(val token: String, val userId: String, val role: String)
