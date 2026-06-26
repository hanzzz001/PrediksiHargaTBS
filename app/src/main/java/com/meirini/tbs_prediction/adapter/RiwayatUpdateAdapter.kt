package com.meirini.tbs_prediction.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meirini.tbs_prediction.R
import com.meirini.tbs_prediction.data.DataRiwayatUpdate

// Adapter
class RiwayatUpdateAdapter(private val listData: ArrayList<DataRiwayatUpdate>) :
    RecyclerView.Adapter<RiwayatUpdateAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDetail: TextView = itemView.findViewById(R.id.tvDetailParameter)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggalUpdate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_update, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listData[position]

        holder.tvDetail.text = "CPO: Rp${data.cpo} | Kernel: Rp${data.kernel}\nIndeks K: ${data.indeksK}%"
        holder.tvTanggal.text = data.tanggalString
    }

    override fun getItemCount(): Int = listData.size
}