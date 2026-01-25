package app.tiebalite.core.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ThemePreferences(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
    private val themeModeKey = "theme_mode"
    private val dynamicColorKey = "dynamic_color"
    private val seedColorKey = "seed_color"

    val settings: Flow<ThemeSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == themeModeKey || key == dynamicColorKey || key == seedColorKey) {
                trySend(readSettings())
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(readSettings())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    fun currentSettings(): ThemeSettings = readSettings()

    fun setThemeModeName(modeName: String) {
        preferences.edit().putString(themeModeKey, modeName).apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        preferences.edit().putBoolean(dynamicColorKey, enabled).apply()
    }

    fun setSeedColor(value: Long) {
        preferences.edit().putLong(seedColorKey, value).apply()
    }

    private fun readSettings(): ThemeSettings {
        val themeModeName =
            preferences.getString(themeModeKey, DefaultThemeModeName) ?: DefaultThemeModeName
        val useDynamicColor = preferences.getBoolean(dynamicColorKey, true)
        val seedColor = preferences.getLong(seedColorKey, DefaultSeedColorLong)
        return ThemeSettings(
            themeModeName = themeModeName,
            useDynamicColor = useDynamicColor,
            seedColor = seedColor
        )
    }
}

data class ThemeSettings(
    val themeModeName: String,
    val useDynamicColor: Boolean,
    val seedColor: Long
)

private const val PreferencesName = "theme_settings"
private const val DefaultThemeModeName = "Light"
private const val DefaultSeedColorLong = 0xFF0F6B5FL
