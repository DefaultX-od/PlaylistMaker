package com.example.playlistmaker

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackListItemAdapter(
    private val tracks: List<Track>,
    private val onClick: (Track) -> Unit
): RecyclerView.Adapter<TrackViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_list_item, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val item = tracks[position]
        holder.bind(tracks[position])
        holder.itemView.setOnClickListener {
            onClick(tracks[position])
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}