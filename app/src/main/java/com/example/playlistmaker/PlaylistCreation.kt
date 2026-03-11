package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class PlaylistCreation : AppCompatActivity() {
    private var imagePath: String = ""

    private var playlistName: String = ""
    private var playlistDescription: String = ""
    private val playlistDao by lazy { (applicationContext as App).database.playlistDao()}

    val playlistDescriptionFiled by lazy { findViewById<TextInputEditText>(R.id.playlistDescriptionField) }
    private val trackMapper = TrackMapper()
    private val playlistCover by lazy { findViewById<ImageView>(R.id.playlistCover) }
    private val playlistCoverContainer by lazy { findViewById<CardView>(R.id.playlistCoverContainer) }
    private val json by lazy {  intent.getStringExtra("EXTRA_TRACK_JSON") }

    private val track by lazy{ Gson().fromJson(json, Track::class.java) }

    private fun showExitDialog(){
        MaterialAlertDialogBuilder(this@PlaylistCreation)
            .setTitle("Завершить создание плейлиста?")
            .setMessage("Все не сохраненные данные будут потеряны")
            .setNegativeButton("Отмена"){
                    dialog, which -> dialog.dismiss()
            }
            .setPositiveButton("Завершить"){
                    dialog, which -> finish()
            }.show()
    }

    private fun prepareExit(){
        playlistDescription = playlistDescriptionFiled.text.toString()
        if(playlistName.isEmpty() && imagePath.isEmpty() && playlistDescription.isEmpty()){
            finish()
        }else{
            showExitDialog()
        }
    }
    private fun saveImageToPrivateStorage(uri: android.net.Uri): String {
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myalbum")
        if (!filePath.exists()) filePath.mkdirs()

        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
            }
        }
        return file.absolutePath
    }
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            playlistCover.setImageURI(uri)
            playlistCoverContainer.alpha = 1f

            imagePath = saveImageToPrivateStorage(uri)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_playlist_creation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_playlist_creation)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this) {
            prepareExit()
        }

        val backButton = findViewById<ImageButton>(R.id.backBtn)
        backButton.setOnClickListener {
            prepareExit()
        }

        playlistCover.setOnClickListener {
            pickMedia.launch("image/*")
        }

        val playlistNameField = findViewById<TextInputEditText>(R.id.playlistNameField)
        val savePlaylistButton = findViewById<MaterialButton>(R.id.saveNewPlaylistButton)

        val textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                playlistName = s.toString()
                savePlaylistButton.isEnabled = s.toString().isNotEmpty()
                savePlaylistButton.alpha = if (s.toString().isNotEmpty()) 1f else 0.5f
            }

            override fun afterTextChanged(s: Editable?) {
                playlistName = s.toString()
                savePlaylistButton.isEnabled = s.toString().isNotEmpty()
                savePlaylistButton.alpha = if (s.toString().isNotEmpty()) 1f else 0.5f
            }
        }

        playlistNameField.addTextChangedListener(textWatcher)

        savePlaylistButton.setOnClickListener {
//            val playlistName = playlistNameField.text.toString()
            playlistDescription = playlistDescriptionFiled.text.toString()
            lifecycleScope.launch {
                val newPlaylistId = playlistDao.insertPlaylist(PlaylistEntity(name=playlistName, description = playlistDescription, imagePath = imagePath?:""))
                Toast.makeText(this@PlaylistCreation, "${getString(R.string.playlist)} \"${playlistName}\" ${getString(R.string.wasCreated)}!", Toast.LENGTH_SHORT).show()
                if(track != null){
                    val playlistTrackEntity = trackMapper.mapToPlaylistEntity(track, newPlaylistId.toInt())
                    playlistDao.insertTrack(playlistTrackEntity)
                    setResult(RESULT_OK)
                    Toast.makeText(this@PlaylistCreation, "${getString(R.string.addedToPlaylist)} \"${playlistName}\"!", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }
}