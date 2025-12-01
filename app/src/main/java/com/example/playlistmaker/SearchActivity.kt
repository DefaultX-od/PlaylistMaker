package com.example.playlistmaker

import com.google.gson.Gson
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.lang.reflect.Type

class SearchActivity : AppCompatActivity() {
    val SEARCH_HISTORY_PREFERENCE = "search_history_preference"
    val HISTORY_TRACK_KEY_3 = "key_for_history_track3"
    val gson = com.google.gson.Gson()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val itunesApiService = retrofit.create<ItunesApiService>()



    private fun loadSearchHistory(sharedPrefs: SharedPreferences): MutableList<String>{
        val trackList = sharedPrefs.getString(HISTORY_TRACK_KEY_3,"") ?: ""

        val type: Type = object : TypeToken<MutableList<String>>() {}.type
        return if (trackList.isEmpty()) {
            mutableListOf()
        } else {
            gson.fromJson(trackList, type)
        }
    }
    private fun saveSearchHistory(track: Track, sharedPrefs: SharedPreferences){
        val maxLength = 10
        val trackList = loadSearchHistory(sharedPrefs)
        val trackStr = gson.toJson(track)

        val existingIndex = trackList.indexOfFirst { json ->
            val t = gson.fromJson(json, Track::class.java)
            t.trackId == track.trackId
        }
        if (existingIndex != -1) {
            trackList.removeAt(existingIndex)
        }
        trackList.add(0, trackStr)
        if (trackList.size > maxLength) {
            trackList.removeAt(9)// Удаляем самый старый (последний) элемент
        }
        val finalJsonToSave = gson.toJson(trackList)
        sharedPrefs.edit {
            putString(HISTORY_TRACK_KEY_3, finalJsonToSave)
        }

        val tracks: List<Track> = trackList.map { json ->
            gson.fromJson(json, Track::class.java)
        }

        val searchHistoryRecycleView = findViewById<RecyclerView>(R.id.searchHistoryList)
        searchHistoryRecycleView.adapter = TrackListItemAdapter(tracks =tracks){ track -> null
        }
    }

    private fun findTracksById( sharedPrefs: SharedPreferences): List<Track>{
        val trackList = loadSearchHistory(sharedPrefs)
        if (trackList.isEmpty()){
            return emptyList<Track>()
        }
        val tracks: List<Track> = trackList.map { json ->
            gson.fromJson(json, Track::class.java)
        }
        return tracks
    }

    private fun updateSearchRecycler(tracks: List<Track>, recycler: RecyclerView, sharedPrefs: SharedPreferences, notFoundWidget: ConstraintLayout){
        if(tracks.isNotEmpty()) {
            recycler.adapter = TrackListItemAdapter(tracks = tracks) { track ->
                saveSearchHistory(track, sharedPrefs)
            }
        }
        else{
            notFoundWidget.visibility = View.VISIBLE
        }
    }
    private fun searchTracks(query: String, recycler: RecyclerView, sharedPrefs: SharedPreferences, notFoundWidget: ConstraintLayout, noConnectionWidget: ConstraintLayout){
        itunesApiService.getTracks(searchStr).enqueue(object : Callback<ItunesResponse>{
            override fun onResponse(call: Call<ItunesResponse>, response: Response<ItunesResponse>) {
                // Получили ответ от сервера
                if (response.isSuccessful) {
                    val itunesResponse = response.body()
                    // Наш запрос был удачным, получаем наши объекты
                    if (itunesResponse != null) {

                        val foundTracks: List<Track> = itunesResponse.results
                        updateSearchRecycler(foundTracks, recycler, sharedPrefs, notFoundWidget)
                    }
                    Log.v("tracks", "Search ended success!!!")

                } else {
                    // Сервер отклонил наш запрос с ошибкой
                    val errorJson = response.errorBody()?.string()
                    Log.v("tracks", errorJson.toString())
                }
            }
            override fun onFailure(call: Call<ItunesResponse>, t: Throwable) {
                noConnectionWidget.visibility = View.VISIBLE
                // Не смогли присоединиться к серверу
                // Выводим ошибку в лог, что-то пошло не так
                t.printStackTrace()
                Log.v("tracks", t.toString())
            }
        }
        )
//        Log.v("tracks", "Я нашел и преобразовал эти треки, перед возвращением: ${found.toString()}")
    }

    private var searchStr: String = QUERY_VAL
    companion object{
        const val SEARCH_QUERY = "SEARCH_QUERY"
        const val QUERY_VAL = ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, searchStr)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchStr = savedInstanceState.getString(SEARCH_QUERY, QUERY_VAL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val sharedPrefs = getSharedPreferences(SEARCH_HISTORY_PREFERENCE, MODE_PRIVATE)

        val noInternetWidget = findViewById<ConstraintLayout>(R.id.noInternetWidget)
        val retrySearchButton = findViewById<Button>(R.id.retrySearchButton)

        val notFoundWidget = findViewById<ConstraintLayout>(R.id.notFoundWidget)

        val searchHistoryRecycleView = findViewById<RecyclerView>(R.id.searchHistoryList)
        val searchResultsRecycleView = findViewById<RecyclerView>(R.id.trackList)
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val backButton = findViewById<ImageButton>(R.id.backBtn)
        val searchFieldClearButton = findViewById<ImageView>(R.id.clearSearchFieldBtn)
        val searchField = findViewById<EditText>(R.id.searchField)
        val clearHistoryButton = findViewById<Button>(R.id.clearHistoryButton)
        val searchHistoryWidget = findViewById<LinearLayout>(R.id.searchHistoryWidget)

        val textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchFieldClearButton.visibility = searchFieldClearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {
                searchStr = s.toString()
                if(searchStr.isEmpty()){
                    searchHistoryWidget.visibility = View.VISIBLE
                    searchResultsRecycleView.visibility = View.GONE
                    notFoundWidget.visibility = View.GONE
                }
            }

        }

        searchHistoryRecycleView.layoutManager = LinearLayoutManager(this)
        searchHistoryRecycleView.adapter = TrackListItemAdapter(tracks = findTracksById(sharedPrefs)){
            track -> null
        }

        searchResultsRecycleView.layoutManager = LinearLayoutManager(this)
        searchResultsRecycleView.adapter = TrackListItemAdapter(tracks = emptyList()){track ->
            this.saveSearchHistory(track, sharedPrefs)
        }

        searchField.setOnFocusChangeListener { v, hasFocus ->
            searchHistoryWidget.visibility=searchHistoryVisibility(sharedPrefs,hasFocus)
            if(!hasFocus){
                inputMethodManager?.hideSoftInputFromWindow(searchField.windowToken, 0)
            }
        }

        searchField.addTextChangedListener(textWatcher)
        if (savedInstanceState != null) {
            searchField.setText(savedInstanceState.getString(SEARCH_QUERY, QUERY_VAL))
        }

        backButton.setOnClickListener {
            finish()
        }

        clearHistoryButton.setOnClickListener {
            searchHistoryWidget.visibility = View.GONE
            sharedPrefs.edit {
                putString(HISTORY_TRACK_KEY_3, "")
            }
            searchHistoryRecycleView.adapter = TrackListItemAdapter(tracks = findTracksById( sharedPrefs)){
                    track -> null
            }
        }

        searchFieldClearButton.setOnClickListener {
            searchField.setText("")
            searchHistoryWidget.visibility = searchHistoryVisibility(sharedPrefs, true)
            searchResultsRecycleView.visibility = View.GONE
            notFoundWidget.visibility = View.GONE
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        retrySearchButton.setOnClickListener {
            searchResultsRecycleView.adapter = TrackListItemAdapter(tracks = emptyList()){}
            notFoundWidget.visibility = View.GONE
            noInternetWidget.visibility = View.GONE
            if (!searchStr.isEmpty()) {
                searchHistoryWidget.visibility = View.GONE
                searchResultsRecycleView.visibility = View.VISIBLE
            }
            else{
                searchHistoryWidget.visibility = View.VISIBLE
                searchResultsRecycleView.visibility = View.GONE
            }
            searchTracks(searchStr, searchResultsRecycleView, sharedPrefs, notFoundWidget, noInternetWidget)
        }

        searchField.setOnEditorActionListener { _, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                searchResultsRecycleView.adapter = TrackListItemAdapter(tracks = emptyList()){}
                notFoundWidget.visibility = View.GONE
                noInternetWidget.visibility = View.GONE
                if (!searchStr.isEmpty()) {
                    searchHistoryWidget.visibility = View.GONE
                    searchResultsRecycleView.visibility = View.VISIBLE
                    searchTracks(searchStr, searchResultsRecycleView, sharedPrefs, notFoundWidget, noInternetWidget)
                    }
                else{
                    searchHistoryWidget.visibility = View.VISIBLE
                    searchResultsRecycleView.visibility = View.GONE
                }
                true
            }
            false
        }
    }

    private fun searchFieldClearButtonVisibility(s: CharSequence?): Int{
        return if (s.isNullOrEmpty()){
            View.GONE
        }
        else{
            View.VISIBLE
        }
    }

    private fun searchHistoryVisibility(sharedPrefs: SharedPreferences, inFocus: Boolean): Int{
        return if(!sharedPrefs.getString(HISTORY_TRACK_KEY_3, "").isNullOrEmpty() and inFocus and (searchStr.isEmpty())){
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }
}