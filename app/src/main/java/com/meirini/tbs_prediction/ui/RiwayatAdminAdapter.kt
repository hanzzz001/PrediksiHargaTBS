package com.meirini.tbs_prediction.ui // Sesuaikan dengan package-mu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meirini.tbs_prediction.R

class RiwayatAdminAdapter(
    private val listRiwayat: ArrayList<Riwayat>,
    private val onDeleteClickListener: (Riwayat) -> Unit // Fungsi pas tombol hapus diklik
) : RecyclerView.Adapter<RiwayatAdminAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTanggal: TextView = view.findViewById(R.id.tvItemTanggal)
        val tvUmur: TextView = view.findViewById(R.id.tvItemUmur)
        val tvIndeksK: TextView = view.findViewById(R.id.tvItemIndeksK)
        val tvCPO: TextView = view.findViewById(R.id.tvItemCPO)
        val tvKernel: TextView = view.findViewById(R.id.tvItemKernel)
        val tvHasilTBS: TextView = view.findViewById(R.id.tvItemHasilTBS)
        val tvHapus: TextView = view.findViewById(R.id.tvItemHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listRiwayat[position]

        // Set teks ke masing-masing kolom tabel harian
        holder.tvTanggal.text = data.tanggal
        holder.tvUmur.text = "${data.umurTanaman} Thn"
        holder.tvIndeksK.text = "${data.indeksK}%"
        holder.tvCPO.text = "Rp ${String.format("%,d", data.hargaCPO)}"
        holder.tvKernel.text = "Rp ${String.format("%,d", data.hargaKernel)}"
        holder.tvHasilTBS.text = "Rp ${String.format("%,d", data.hasilTBS)}"

        // Aksi pas teks "Hapus" warna merah diklik
        holder.tvHapus.setOnClickListener {
            onDeleteClickListener(data)
        }
    }

    override fun getItemCount(): Int = listRiwayat.size
}