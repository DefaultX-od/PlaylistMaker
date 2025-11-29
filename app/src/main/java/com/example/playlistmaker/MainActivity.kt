package com.example.playlistmaker

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    val SETINGS_PREFERENCES = "user_settings_preferences"
    val DARKMODE_ENABLE_KEY = "key_for_darkmode"

    fun applySettings(settingsPrefs: SharedPreferences){
        val isDarkMode = settingsPrefs.getBoolean(DARKMODE_ENABLE_KEY,false)
        if(isDarkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val settingsPrefs = getSharedPreferences(SETINGS_PREFERENCES, MODE_PRIVATE)

        this.applySettings(settingsPrefs)

        //Анонимный класс
        val searchButton = findViewById<Button>(R.id.searchButton)
        val searchButtonClickListener : View.OnClickListener = object : View.OnClickListener{
            override fun onClick(v: View?) {
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivity(intent)
//                Toast.makeText(this@MainActivity, "Вы нажали на кнопку Поиска!", Toast.LENGTH_SHORT).show()
            }
        }
        searchButton.setOnClickListener(searchButtonClickListener)

        //Лямбда выражение
        val mediatekaButton = findViewById<Button>(R.id.mediatekaButton)
        mediatekaButton.setOnClickListener {
            val intent = Intent(this, MediatekaActivity::class.java)
            startActivity(intent)
//            Toast.makeText(this@MainActivity, "Вы нажали на кнопку Медиатека!", Toast.LENGTH_SHORT).show()
        }

        //Лямбда выражение
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
