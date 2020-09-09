package com.merpati.durgence.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.andresaftari.durgence.R
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.merpati.durgence.model.FirstAid
import kotlinx.android.synthetic.main.list_firstaid.view.*

class FirstAidAdapter(private val list: ArrayList<FirstAid>) :
    RecyclerView.Adapter<FirstAidAdapter.AidViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AidViewHolder =
        AidViewHolder((LayoutInflater.from(parent.context)), parent)

    override fun onBindViewHolder(holder: AidViewHolder, position: Int) {
        holder.bind(list[position])

        val data = list[position]
        holder.itemView.setOnClickListener {
            Snackbar.make(holder.itemView, "Selected $data", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = list.size

    class AidViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_firstaid, parent, false)) {

        fun bind(firstAid: FirstAid) {
            with(itemView) {
                Glide.with(context)
                    .load(firstAid.thumbnail)
                    .into(iv_aidThumb)

                tv_aidName?.text = firstAid.nameIndo
            }
        }
    }
}