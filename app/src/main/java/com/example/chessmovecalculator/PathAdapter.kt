package com.example.chessmovecalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PathAdapter(private var paths: List<String>) : RecyclerView.Adapter<PathAdapter.PathViewHolder>() {

    class PathViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pathTextView: TextView = itemView.findViewById(R.id.pathTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.path_item, parent, false)
        return PathViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PathViewHolder, position: Int) {
        holder.pathTextView.text = paths[position]
    }

    override fun getItemCount() = paths.size

    fun updatePaths(newPaths: List<String>) {
        paths = newPaths
        notifyDataSetChanged()
    }
}