package com.runpassport.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataSource: DataStore<Preferences> by preferencesDataStore(name = "auth")

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessTokenKey = stringPreferencesKey("access_token")

    suspend fun saveAccessToken(token: String) {
        context.dataSource.edit { preferences ->
            preferences[accessTokenKey] = token
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataSource.data.map { preferences ->
            preferences[accessTokenKey]
        }.first()
    }

    val accessTokenFlow: Flow<String?> = context.dataSource.data.map { preferences ->
        preferences[accessTokenKey]
    }

    suspend fun clearAccessToekn() {
        context.dataSource.edit { preferences ->
            preferences.remove(accessTokenKey)
        }
    }
}