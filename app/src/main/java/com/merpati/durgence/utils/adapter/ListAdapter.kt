package com.merpati.durgence.utils.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListAdapter<T>(
    var layout: Int,
    private var itemList: List<T>,
    var view: (View, T) -> Unit,
    var handler: (Int, T) -> Unit
) : RecyclerView.Adapter<ListAdapter.ViewHolder<T>>() {

    var data = this.itemList
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> =
        ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(this.layout, parent, false)
        )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        val item: T = data[position]
        holder.apply {
            bind(item, view)
            itemView.setOnClickListener { handler(position, item) }
        }
    }

    class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: T, view: (View, T) -> Unit) = view(itemView, item)
    }
}