package com.example.playlistmaker

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import com.google.gson.Gson

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
    private val playlistDao by lazy { (applicationContext as App).database.playlistDao()}
    private val json by lazy {  intent.getStringExtra("EXTRA_TRACK_JSON") }
    private val track by lazy{ Gson().fromJson(json, Track::class.java) }
    private val mediaPlayer = MediaPlayer()
    private val overlay by lazy { findViewById<View>(R.id.overlay) }
    private val bottomSheetContainer by lazy { findViewById<LinearLayout>(R.id.playlists_bottom_sheet) }
    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from<LinearLayout>(
            bottomSheetContainer
        )
    }

    private val playlistCreationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == Activity.RESULT_OK){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    companion object{
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DEBOUNCE_DELAY = 500L
    }

    private fun addTrackToPlaylist(playlistId: Int, playlistName: String){
        val trackPlaylistEntity = trackMapper.mapToPlaylistEntity(track, playlistId)

        lifecycleScope.launch {
            val res = playlistDao.insertTrack(trackPlaylistEntity)
            if(res == -1L){
                Toast.makeText(this@PlayerActivity, "${getString(R.string.trackAlreadyInPlaylist)} \"${playlistName}\".", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@PlayerActivity, "${getString(R.string.addedToPlaylist)} \"${playlistName}\".", Toast.LENGTH_SHORT).show()
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                updatePlaylistList()
            }

        }

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

    private fun updatePlaylistList(){
        val playlistList = findViewById<RecyclerView>(R.id.playlistsList)
        playlistList.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val playlists = playlistDao.getPlaylists()
            playlistList?.adapter = PlaylistListItemAdapter (playlists = playlists){
                    playlist -> addTrackToPlaylist(playlist.playlist.playlistId, playlist.playlist.name)
            }
        }
    }

    private var playerState = STATE_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        updatePlaylistList()

        val createNewPlaylistButton = findViewById<Button>(R.id.createNewPlaylistButton)

        createNewPlaylistButton.setOnClickListener {
            val trackJson = Gson().toJson(track)
            val intent = Intent(this, PlaylistCreation::class.java).apply {
                putExtra("EXTRA_TRACK_JSON", trackJson)
            }
            playlistCreationLauncher.launch(intent)
        }

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val addToPlaylistButton = findViewById<MaterialButton>(R.id.addToPlaylistButton)
        addToPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                overlay.visibility = if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                overlay.alpha = slideOffset + 1f
            }
        })

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
            handler.removeCallbacks(progressRunnable)
            timer.text = "00:00"
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        updatePlaylistList()
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