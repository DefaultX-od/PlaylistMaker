package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PlaylistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PlaylistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var playlists: List<PlaylistWithCount> = emptyList()
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newPlaylistButton = view.findViewById<MaterialButton>(R.id.createNewPlaylistButton)
        newPlaylistButton.setOnClickListener {
            val intent = Intent(requireContext(), PlaylistCreation::class.java)
            startActivity(intent)
        }
        updatePlaylistRecycleView()
    }

    override fun onResume(){
        super.onResume()
        updatePlaylistRecycleView()
    }

    private fun updatePlaylistRecycleView(){
        super.onResume()

        val playlistDao = (requireContext().applicationContext as App).database.playlistDao()
        val playlistGrid = view?.findViewById<RecyclerView>(R.id.playlistGrid)

        lifecycleScope.launch {

            playlists = playlistDao.getPlaylists()
            val emptyGridWidget = view?.findViewById<ConstraintLayout>(R.id.emptyGridWidget)
            val emptyWidgetText = view?.findViewById<TextView>(R.id.emptyWidgetText)

            if(playlists.isNotEmpty()){
                emptyGridWidget?.visibility = View.GONE
            }
            else{
                emptyWidgetText?.text = getString(R.string.playlistsEmpty)
                emptyGridWidget?.visibility = View.VISIBLE
            }

            playlistGrid?.layoutManager = GridLayoutManager(requireContext(), 2)
            playlistGrid?.adapter = PlaylistGridItemAdapter (playlists = playlists){
                playlist ->
                val playlistJson = Gson().toJson(playlist)
                val intent = Intent(requireContext(), PlaylistActivity::class.java).apply {
                    putExtra("EXTRA_PLAYLIST_JSON", playlistJson)
                }
                startActivity(intent)
            }

        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PlaylistFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}