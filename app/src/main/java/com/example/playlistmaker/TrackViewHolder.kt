package com.example.playlistmaker

import android.icu.text.SimpleDateFormat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val trackImgView: ImageView
    private val trackNameView: TextView
    private val trackArtistNameView: TextView
    private val trackLengthView: TextView
    init {
        trackImgView = itemView.findViewById(R.id.trackImgView)
        trackNameView = itemView.findViewById(R.id.trackNameView)
        trackArtistNameView = itemView.findViewById(R.id.trackArtistNameView)
        trackLengthView = itemView.findViewById(R.id.trackLengthView)
    }
    fun bind(model: Track){
        Glide.with(itemView).load(model.artworkUrl100).placeholder(R.drawable.art_placeholder).into(trackImgView)
        trackNameView.text = model.trackName
        trackArtistNameView.text = model.artistName
        trackLengthView.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(model.trackTimeMillis)
    }

}