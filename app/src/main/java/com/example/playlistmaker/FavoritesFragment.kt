package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



/**
 * A simple [Fragment] subclass.
 * Use the [FavoritesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoritesFragment : Fragment() {
    // TODO: Rename and change types of parameters
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
        return inflater.inflate(R.layout.fragment_favorites, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.emptyWidgetText).text = getString(R.string.favoritesEmpty)
        updateFavoriteRecycleView()
    }

    override fun onResume() {
        super.onResume()
        updateFavoriteRecycleView()
    }

    private fun updateFavoriteRecycleView(){
        super.onResume()

        val favoritesRecycleView = view?.findViewById<RecyclerView>(R.id.favoriteTrackList)
        val trackMapper = TrackMapper()
        val favoriteTrackDao = (requireContext().applicationContext as App).database.favoriteTrackDao()

        lifecycleScope.launch {
            val favoriteTracksEntity = favoriteTrackDao.getFavoriteTracks()
            val tracks = favoriteTracksEntity.map { entity -> trackMapper.map(entity) }

            if (tracks.isNotEmpty()){
                view?.findViewById<ConstraintLayout>(R.id.emptyListWidget)?.visibility = View.GONE
            }
            else{
                view?.findViewById<ConstraintLayout>(R.id.emptyListWidget)?.visibility = View.VISIBLE
            }

            favoritesRecycleView?.layoutManager = LinearLayoutManager(requireContext())
            favoritesRecycleView?.adapter = TrackListItemAdapter(tracks = tracks){ track ->
                val trackJson = Gson().toJson(track)
                val intent = Intent(requireContext(), PlayerActivity::class.java).apply{
                    putExtra("EXTRA_TRACK_JSON", trackJson)
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
         * @return A new instance of fragment FavoritesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoritesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}