package com.joaaofiilho.animationpoc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.holder_video.view.*

class VideosAdapter(private val onItemClicked: (transitionView: View) -> Unit): RecyclerView.Adapter<VideosAdapter.VideoViewHolder>() {

    private val items = mutableListOf<String>()

    fun update(items: List<String>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.holder_video, parent, false)
        return VideoViewHolder(view, onItemClicked)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class VideoViewHolder(view: View, private val onItemClicked: (transitionView: View) -> Unit): RecyclerView.ViewHolder(view) {
        fun bind(item: String) {
            itemView.textTitulo.text = item
            itemView.setOnClickListener {
                onItemClicked(itemView.thumbnail)
            }
        }
    }
}