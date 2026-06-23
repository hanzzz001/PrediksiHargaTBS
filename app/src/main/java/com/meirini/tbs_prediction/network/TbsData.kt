package com.meirini.tbs_prediction.network

import com.google.gson.annotations.SerializedName

// 1. Data yang bakal dikirim Android ke Server (Input Admin)
data class TbsRequest(
    @SerializedName("harga_cpo")
    val cpo: Int,

    @SerializedName("harga_kernel")
    val kernel: Int,

    @SerializedName("indeks_k")
    val indeksK: Double
)

// 2. Data kembalian dari Server ke Android (13 Daftar Harga)
data class TbsResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data_prediksi")
    val dataPrediksi: List<HargaPrediksi>
)

// 3. Struktur per item harga dari ke-13 umur tersebut
data class HargaPrediksi(
    @SerializedName("umur_tanaman")
    val umurTanaman: Int, // Contoh: 3, 4, 5... (10 untuk 10-20 tahun)

    @SerializedName("harga_tbs")
    val hargaTbs: Double
)