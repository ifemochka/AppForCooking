package com.example.appforcooking.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val FIRST_NAME_KEY = stringPreferencesKey("first_name")
        private val LAST_NAME_KEY = stringPreferencesKey("last_name")
    }

    suspend fun saveAuthData(
        token: String,
        userId: Long,
        email: String,
        firstName: String,
        lastName: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = email
            preferences[FIRST_NAME_KEY] = firstName
            preferences[LAST_NAME_KEY] = lastName
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun getTokenBlocking(): String? {
        return getToken().first()
    }

    fun getUserId(): Flow<Long?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun getUserIdBlocking(): Long {
        return getUserId().first() ?: 0
    }

    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    fun getUserName(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            val firstName = preferences[FIRST_NAME_KEY] ?: ""
            val lastName = preferences[LAST_NAME_KEY] ?: ""
            if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                "$firstName $lastName".trim()
            } else {
                preferences[USER_EMAIL_KEY] ?: "Пользователь"
            }
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val token = getToken().first()
        return !token.isNullOrEmpty()
    }
}