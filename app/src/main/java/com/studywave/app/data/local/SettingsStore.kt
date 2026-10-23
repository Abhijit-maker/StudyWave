package com.studywave.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("studywave_settings")

class SettingsStore(private val context: Context) {
    private val onboarding = booleanPreferencesKey("onboarding_complete")
    private val language = stringPreferencesKey("language")

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { it[onboarding] ?: false }
    val preferredLanguage: Flow<String> = context.dataStore.data.map { it[language] ?: "en" }

    suspend fun setOnboardingComplete(value: Boolean) {
        context.dataStore.edit { it[onboarding] = value }
    }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[language] = value }
    }
}
