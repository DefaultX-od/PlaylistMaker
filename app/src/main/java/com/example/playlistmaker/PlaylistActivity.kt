package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.launch

class PlaylistActivity : AppCompatActivity() {
    private val trackMapper = TrackMapper()
    private val playlistDao by lazy { (applicationContext as App).database.playlistDao()}
    private val json by lazy { intent.getStringExtra("EXTRA_PLAYLIST_JSON") }
    private val playlist by lazy { Gson().fromJson(json, PlaylistWithCount::class.java) }

    private val trackListRecycler by lazy { findViewById<RecyclerView>(R.id.trackList) }

    private fun loadTracks(){
        lifecycleScope.launch {
            val tracksPlaylistEntity = playlistDao.getTracksByPlaylistId(playlist.playlist.playlistId)
            val tracks = tracksPlaylistEntity.map { entity -> trackMapper.map(entity) }

            if (tracks.isNotEmpty()){

                findViewById<ConstraintLayout>(R.id.emptyListWidget)?.visibility = View.GONE
            }
            else{
                findViewById<ConstraintLayout>(R.id.emptyListWidget)?.visibility = View.VISIBLE
            }

            trackListRecycler.layoutManager = LinearLayoutManager(this@PlaylistActivity)
            trackListRecycler.adapter = TrackListItemAdapter(tracks = tracks){
                track ->
                val trackJson = Gson().toJson(track)
                val intent = Intent(this@PlaylistActivity, PlayerActivity::class.java).apply{
                    putExtra("EXTRA_TRACK_JSON", trackJson)
                }
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_playlist)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.playlistNameHeader).text = playlist.playlist.name
        findViewById<TextView>(R.id.emptyWidgetText).text = getString(R.string.playlistEmpty)
        val backButton = findViewById<ImageButton>(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }

        loadTracks()
    }
}