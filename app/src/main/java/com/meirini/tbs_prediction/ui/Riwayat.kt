package com.meirini.tbs_prediction.ui // Sesuaikan package-mu ya

data class Riwayat(
    var id: String = "", // Ini buat nyimpan ID Dokumen Firebase (berguna pas mau menghapus data)
    var tanggal: String = "",
    var umurTanaman: Int = 0,
    var indeksK: Double = 0.0,
    var hargaCPO: Int = 0,
    var hargaKernel: Int = 0,
    var hasilTBS: Int = 0 // Ini nilai dummy (0) dulu sambil nunggu API ML dari Mei
)