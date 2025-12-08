package com.example.primerprototipo.model

import java.util.Date

data class UbicacionAutobus(
    val autobusId: String = "",
    val choferId: String = "",
    val ruta: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val proximaParada: String = "",
    val pasajerosAbordo: Int = 0,
    val timestamp: Date = Date(),
    val velocidad: Float = 0f,
    val precision: Float = 0f
)
