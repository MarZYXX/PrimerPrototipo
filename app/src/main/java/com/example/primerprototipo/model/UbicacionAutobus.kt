package com.example.primerprototipo.model

import java.util.Date

data class UbicacionAutobus(
    val autobusId: String,
    val choferId: String,
    val ruta: String, // "Misantla-Martinez" o "Martinez-Misantla"
    val latitud: Double,
    val longitud: Double,
    val velocidad: Float = 0f,
    val precision: Float = 0f,
    val timestamp: Date = Date(),
    val proximaParada: String = "",
    val pasajerosAbordo: Int = 0,
    val enRuta: Boolean = true
) {
    constructor() : this("", "", "", 0.0, 0.0)
}