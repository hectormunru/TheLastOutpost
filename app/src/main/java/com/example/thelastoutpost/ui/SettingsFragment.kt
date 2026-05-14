package com.example.thelastoutpost.ui

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.example.thelastoutpost.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "game_settings"
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Listener para el brillo (ejemplo)
        val brightnessPref = findPreference<androidx.preference.SeekBarPreference>("brightness")
        brightnessPref?.setOnPreferenceChangeListener { _, newValue ->
            val layoutParams = activity?.window?.attributes
            layoutParams?.screenBrightness = (newValue as Int) / 100f
            activity?.window?.attributes = layoutParams
            true
        }
    }
}


