package com.meirini.tbs_prediction.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meirini.tbs_prediction.R
import com.meirini.tbs_prediction.ui.LogPanen
import java.text.NumberFormat
import java.util.Locale

class LogPanenAdapter(private val listLog: List<LogPanen>) :
    RecyclerView.Adapter<LogPanenAdapter.LogViewHolder>() {

    // Menyambungkan ID dari item_riwayat_admin_baris.xml
    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNama: TextView = view.findViewById(R.id.tvKolomNama)
        val tvBruto: TextView = view.findViewById(R.id.tvKolomBruto)
        val tvPotongan: TextView = view.findViewById(R.id.tvKolomPotongan)
        val tvNetto: TextView = view.findViewById(R.id.tvKolomNetto)

        fun bind(log: LogPanen) {
            val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
            }

            tvNama.text = log.namaPetani
            tvBruto.text = "${log.bruto} Kg"
            tvPotongan.text = formatRupiah.format(log.biaya)
            tvNetto.text = formatRupiah.format(log.netto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_riwayat_admin_baris, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(listLog[position])
    }

    override fun getItemCount(): Int = listLog.size
}