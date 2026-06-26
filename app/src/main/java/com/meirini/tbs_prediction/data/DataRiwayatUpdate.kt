package com.meirini.tbs_prediction.data

data class DataRiwayatUpdate(
    val tanggalString: String,
    val cpo: Int,
    val kernel: Int,
    val indeksK: Double,
    val timestamp: Long
)