package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    val SETINGS_PREFERENCES = "user_settings_preferences"
    val DARKMODE_ENABLE_KEY = "key_for_darkmode"

    fun restoreSettings(settingsPrefs: SharedPreferences){
        val isDarkModeSwitch = findViewById<Switch>(R.id.isDarkModeSwitch)
        val isDarkMode = settingsPrefs.getBoolean(DARKMODE_ENABLE_KEY, false)
        if(isDarkMode){
            isDarkModeSwitch.isChecked = true
        }
        else{
            isDarkModeSwitch.isChecked = false
        }
    }

    fun applySettings(settingsPrefs: SharedPreferences){
        val isDarkMode = settingsPrefs.getBoolean(DARKMODE_ENABLE_KEY, false)
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        recreate()
    }

    fun saveSettings(settingsPrefs: SharedPreferences){
        val isDarkModeSwitch = findViewById<Switch>(R.id.isDarkModeSwitch)
        settingsPrefs.edit {
            putBoolean(DARKMODE_ENABLE_KEY, isDarkModeSwitch.isChecked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        val settingsPrefs = getSharedPreferences(SETINGS_PREFERENCES, MODE_PRIVATE)
        restoreSettings(settingsPrefs)

        val isDarkModeSwitch = findViewById<Switch>(R.id.isDarkModeSwitch)
        isDarkModeSwitch.setOnClickListener {
            saveSettings(settingsPrefs)
            applySettings(settingsPrefs)
        }

        //Лямбда выражение
        val backButton = findViewById< ImageButton>(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }

        val shareBtn = findViewById<Button>(R.id.shareBtn)
        shareBtn.setOnClickListener {
            val msg = getString(R.string.myGroupName)
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            startActivity(Intent.createChooser(intent, null))
        }

        val supportButton = findViewById<Button>(R.id.supportBtn)
        supportButton.setOnClickListener {
            val msg = getString(R.string.contactDevsMsgPlaceholder)
            val subject = getString(R.string.emailSubjectPlaceholder)
            val devEmail = getString(R.string.devEmail)
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = "mailto:".toUri()
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(devEmail))
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            startActivity(intent)
        }

        val licenceButton = findViewById<Button>(R.id.licenceBtn)
        licenceButton.setOnClickListener {
            val link = getString(R.string.licenceLink)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = link.toUri()
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}