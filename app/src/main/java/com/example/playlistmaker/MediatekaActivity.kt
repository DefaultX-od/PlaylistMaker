package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MediatekaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mediateka)

        //Лямбда выражение
        val backButton = findViewById< ImageButton>(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutMediateka)
        val viewPager = findViewById<ViewPager2>(R.id.pager)

        viewPager.adapter = MediatekaAdapter(this)

        TabLayoutMediator(tabLayout, viewPager){tab, position ->
            tab.text = when(position){
                0 -> getString(R.string.favoritesTab)
                1 -> getString(R.string.playlistsTab)
                else -> null
            }
        }.attach()



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mediateka)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}