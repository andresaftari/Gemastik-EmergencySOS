package com.merpati.durgence.utils.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.andresaftari.durgence.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.merpati.durgence.model.Services
import com.merpati.durgence.views.ui.activity.main.HospitalActivity
import kotlinx.android.synthetic.main.list_service.view.*

class ServiceAdapter(private val list: ArrayList<Services>) :
    RecyclerView.Adapter<ServiceAdapter.ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder =
        ListViewHolder(LayoutInflater.from(parent.context), parent)

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(list[position])
        val data = list[position]

        when (position % 3) {
            0 -> {
                holder.itemView.apply {
                    mcv_services.setCardBackgroundColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.colorHospital
                        )
                    )
                    setOnClickListener {
                        holder.itemView.context.startActivity(
                            Intent(
                                holder.itemView.context,
                                HospitalActivity::class.java
                            )
                        )
                    }
                }
            }
            1 -> {
                holder.itemView.apply {
                    mcv_services.setCardBackgroundColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.colorPolice
                        )
                    )
                    setOnClickListener {
                        Snackbar.make(
                            holder.itemView.mcv_services,
                            "Selected ${data.nameEng}!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            2 -> {
                holder.itemView.apply {
                    mcv_services.setCardBackgroundColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.colorFire
                        )
                    )
                    setOnClickListener {
                        Snackbar.make(
                            holder.itemView.mcv_services,
                            "Selected ${data.nameEng}!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = list.size

    class ListViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.list_service, parent, false)) {

        fun bind(services: Services) {
            with(itemView) {
                Glide.with(context)
                    .load(services.thumbnail)
                    .apply(RequestOptions().override(60, 60))
                    .into(iv_serviceType)

                tv_serviceIndo?.text = services.nameIndo
                tv_serviceEng?.text = services.nameEng
            }
        }
    }
}