package com.example.primerprototipo.model

import java.io.Serializable

data class Autobus(
    val id: String,
    val numeroUnidad: String,
    var ruta: String,
    var pasajerosAbordo: Int = 0,
    var proximaParada: String = "",
    var tiempoSalida: String = "",
    var latitud: Double = 0.0,
    var longitud: Double = 0.0,
    var enRuta: Boolean = false
) : Serializable {

    companion object {
        const val CAPACIDAD_MAXIMA = 40
    }

    fun estaLleno(): Boolean = pasajerosAbordo >= CAPACIDAD_MAXIMA

    fun agregarPasajero(): Boolean {
        return if (pasajerosAbordo < CAPACIDAD_MAXIMA) {
            pasajerosAbordo++
            true
        } else {
            false
        }
    }

    fun quitarPasajero(): Boolean {
        return if (pasajerosAbordo > 0) {
            pasajerosAbordo--
            true
        } else {
            false
        }
    }

    fun obtenerCapacidadDisponible(): Int = CAPACIDAD_MAXIMA - pasajerosAbordo
}
