package io.github.francoiscampbell.xposeddatausage.nonmodule

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import io.github.francoiscampbell.xposeddatausage.R
import io.github.francoiscampbell.xposeddatausage.log.XposedLog
import io.github.francoiscampbell.xposeddatausage.util.putAnyExtra

/**
 * Created by francois on 16-03-17.
 */
class SettingsChangeActions(
        private val context: Context
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    private val res = context.resources
    private val settingsUpdatedAction = res.getString(R.string.action_settings_updated)

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val newPrefValue = sharedPreferences.all[key]
        XposedLog.d("$key changed to $newPrefValue in ${javaClass.simpleName}")

        if (isPrefForApp(key)) {
            handleAppPrefChange(key, newPrefValue)
        } else {
            handlePrefChange(key, newPrefValue)
        }

        //handle debug logging for main app
        if (key == res.getString(R.string.pref_debug_logging_key)) {
            XposedLog.debugLogging = newPrefValue as Boolean
        }
    }

    fun startListeningForChanges() {
        XposedLog.d("startBroadcastingChanges")
        prefs.all.forEach { onSharedPreferenceChanged(prefs, it.key) } //push settings to module when opening the settings app
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    fun stopListeningForChanges() {
        XposedLog.d("stopBroadcastingChanges")
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun isPrefForApp(key: String) = key.startsWith("app_")

    private fun handleAppPrefChange(key: String, newValue: Any?) = when (key) {
        res.getString(R.string.pref_app_show_in_launcher_key) -> onShowInLauncherChanged(newValue as Boolean)
        else -> {
        }
    }

    private fun handlePrefChange(key: String, newPrefValue: Any?) {
        @Suppress("UNCHECKED_CAST")
        val sentPrefValue = when (newPrefValue) {
            is Set<*> -> (newPrefValue as Set<String>).toTypedArray()
            else -> newPrefValue
        }
        context.sendBroadcast(Intent(settingsUpdatedAction).putAnyExtra(key, sentPrefValue))
    }

    private fun onShowInLauncherChanged(showInLauncher: Boolean) {
        val newStatus = if (showInLauncher) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        val componentName = ComponentName(context, SettingsActivity::class.java.canonicalName + "-Alias")
        context.packageManager.setComponentEnabledSetting(componentName, newStatus, PackageManager.DONT_KILL_APP)
    }
}