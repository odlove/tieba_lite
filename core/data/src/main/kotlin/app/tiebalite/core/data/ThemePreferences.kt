package app.tiebalite.core.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_settings")

class ThemePreferences(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")
    private val seedColorKey = longPreferencesKey("seed_color")

    val settings: Flow<ThemeSettings> = context.dataStore.data.map { prefs ->
        ThemeSettings(
            themeModeName = prefs[themeModeKey] ?: DefaultThemeModeName,
            useDynamicColor = prefs[dynamicColorKey] ?: true,
            seedColor = prefs[seedColorKey] ?: DefaultSeedColorLong
        )
    }

    suspend fun setThemeModeName(modeName: String) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = modeName
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[dynamicColorKey] = enabled
        }
    }

    suspend fun setSeedColor(value: Long) {
        context.dataStore.edit { prefs ->
            prefs[seedColorKey] = value
        }
    }
}

data class ThemeSettings(
    val themeModeName: String,
    val useDynamicColor: Boolean,
    val seedColor: Long
)

private const val DefaultThemeModeName = "Light"
private const val DefaultSeedColorLong = 0xFF0F6B5FL
