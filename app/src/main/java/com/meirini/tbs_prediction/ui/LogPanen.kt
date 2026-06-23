package com.meirini.tbs_prediction.ui

data class LogPanen(
    val id: String = "",
    val namaPetani: String = "",
    val bruto: Double = 0.0,
    val biaya: Double = 0.0,
    val netto: Double = 0.0,
    val timestamp: Long = 0L
)