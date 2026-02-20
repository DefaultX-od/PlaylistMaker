package com.example.playlistmaker

import android.media.MediaPlayer
import com.google.gson.Gson

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private var isFavorite = false
    private val trackMapper = TrackMapper()
    private val favoriteTrackDao by lazy { (applicationContext as App).database.favoriteTrackDao() }
    private val json by lazy {  intent.getStringExtra("EXTRA_TRACK_JSON") }
    private val track by lazy{ Gson().fromJson(json, Track::class.java) }
    private val mediaPlayer = MediaPlayer()
    companion object{
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DEBOUNCE_DELAY = 500L
    }

    private val handler = Handler(Looper.getMainLooper())

    private val progressRunnable: Runnable = Runnable {
        if (playerState == STATE_PLAYING) {
            val timer = findViewById<TextView>(R.id.timer)
            val trackProgressBar = findViewById<Slider>(R.id.trackProgressBar)

            trackProgressBar.value = mediaPlayer.currentPosition.toFloat()
            timer.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
            handler.postDelayed(progressRunnable, TIMER_UPDATE_DEBOUNCE_DELAY)
        }
    }

    private var playerState = STATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        lifecycleScope.launch {
            isFavorite = favoriteTrackDao.isFavorite(track.trackId)
            updateFavoriteButton()
        }

        findViewById<TextView>(R.id.track_name).text = track.trackName
        findViewById<TextView>(R.id.track_author).text = track.artistName

        Glide.with(this)
            .load(track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg"))
            .placeholder(R.drawable.art_placeholder)
            .into(findViewById<ImageView>(R.id.track_cover))

        findViewById<TextView>(R.id.track_length).text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)

        findViewById<TextView>(R.id.track_collection).text = track.collectionName

        findViewById<TextView>(R.id.track_year).text = track.releaseDate.substringBefore("-")

        findViewById<TextView>(R.id.track_genre).text = track.primaryGenreName

        findViewById<TextView>(R.id.track_country).text = track.country

        val timer = findViewById<TextView>(R.id.timer)

        //Лямбда выражение
        val backButton = findViewById< ImageButton>(R.id.backBtn)
        backButton.setOnClickListener {
            finish()
        }

        val mediaControlButton = findViewById<MaterialButton>(R.id.media_control_button)

        val favoriteButton = findViewById<MaterialButton>(R.id.favoriteButton)

        mediaControlButton.setOnClickListener {
            playBackControl(mediaControlButton)
        }

        favoriteButton.setOnClickListener {
            updateFavoriteState()
        }

        preparePlayer(track.previewUrl)

        mediaPlayer.setOnPreparedListener {
            findViewById<ProgressBar>(R.id.trackLoadProgressBar).visibility = View.GONE
            mediaControlButton.visibility = View.VISIBLE
            mediaControlButton.isEnabled = true
            playerState = STATE_PREPARED
        }

        mediaPlayer.setOnCompletionListener {
            mediaControlButton.setIconResource(R.drawable.play)
            val trackProgressBar = findViewById<Slider>(R.id.trackProgressBar)

            trackProgressBar.value = 30000.toFloat()
            playerState = STATE_PREPARED
            handler.removeCallbacks(progressRunnable) // Стоп таймер
            timer.text = "00:00"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(progressRunnable)
    }

    private fun updateFavoriteButton(){
        val favoriteButton = findViewById<MaterialButton>(R.id.favoriteButton)
        if (isFavorite){
            favoriteButton.setIconResource(R.drawable.like_active)
            favoriteButton.iconTint = null
        }
        else{
            favoriteButton.setIconResource(R.drawable.like_inactive)
            favoriteButton.setIconTintResource(R.color.fg_inverted)
        }
    }
    private fun updateFavoriteState(){
        lifecycleScope.launch {
            val favoriteEntity = trackMapper.map(track)

            if(isFavorite){
                favoriteTrackDao.deleteTrack(favoriteEntity)
                isFavorite = false
            }
            else{
                favoriteTrackDao.insertTrack(favoriteEntity)
                isFavorite = true
            }
            updateFavoriteButton()
        }
    }
    private fun playBackControl(mediaBtn : MaterialButton){
        when(playerState){
            STATE_PLAYING ->{
                pausePlayer(mediaBtn)
            }
            STATE_PREPARED, STATE_PAUSED ->{
                startPlayer(mediaBtn)
            }
        }
    }

    private fun startPlayer(mediaBtn : MaterialButton){
        mediaPlayer.start()
        mediaBtn.setIconResource(R.drawable.pause)
        playerState = STATE_PLAYING
        handler.post(progressRunnable)
    }

    private fun pausePlayer(mediaBtn : MaterialButton){
        mediaPlayer.pause()
        playerState = STATE_PAUSED
        mediaBtn.setIconResource(R.drawable.play)
        handler.removeCallbacks(progressRunnable)
    }

    private fun preparePlayer(url: String){
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
    }
}