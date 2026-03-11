package com.example.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val playlistNameView: TextView
    private val trackCountView: TextView
    private val playlistImgView: ImageView
    init {
        playlistNameView = itemView.findViewById(R.id.playlistNameView)
        trackCountView = itemView.findViewById(R.id.trackCountView)
        playlistImgView = itemView.findViewById(R.id.playlistImgView)
    }
    fun bind(model: PlaylistWithCount){
        Glide.with(itemView).load(model.playlist.imagePath).placeholder(R.drawable.art_placeholder).into(playlistImgView)
        playlistNameView.text = model.playlist.name
        trackCountView.text = model.trackCount.toString()
    }

}