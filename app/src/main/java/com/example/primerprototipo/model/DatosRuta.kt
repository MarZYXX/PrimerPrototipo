package com.example.primerprototipo.model

import com.google.android.gms.maps.model.LatLng

// Representa una parada o punto de interés en la ruta.
data class Parada(
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)

// Contiene la información completa de una ruta dinámica calculada por la API.
data class Ruta(
    val polyline: List<LatLng>,
    val paradas: List<Parada>
)

// Define las terminales principales de la ruta.
enum class Terminal(val nombreCompleto: String) {
    MISANTLA("Terminal Misantla"),
    MARTINEZ("Terminal Martínez de la Torre");

    override fun toString(): String = nombreCompleto
}

// Objeto de utilidad para gestionar la información de las rutas.
object RutasMisantla {

    // Direcciones exactas para que la API de Google Maps calcule la ruta correcta.
    private const val DIRECCION_TERMINAL_MISANTLA = "ADO Misantla Ezequiel Alatriste 1120-122, Centro, 93820 Misantla, Ver."
    private const val DIRECCION_TERMINAL_MARTINEZ = "ADO Martinez de la Torre Melchor Ocampo 501 Ote, Centro, 93600 Martínez de la Torre, Ver."

    // Devuelve un nombre descriptivo para la ruta (ej. "Misantla → Martínez").
    fun obtenerNombreRuta(terminal: Terminal): String {
        return when (terminal) {
            Terminal.MISANTLA -> "Misantla → Martínez"
            Terminal.MARTINEZ -> "Martínez → Misantla"
        }
    }

    // Devuelve la dirección de la terminal de destino según la terminal de salida.
    fun obtenerTerminalDestino(terminal: Terminal): String {
        return when (terminal) {
            Terminal.MISANTLA -> DIRECCION_TERMINAL_MARTINEZ
            Terminal.MARTINEZ -> DIRECCION_TERMINAL_MISANTLA
        }
    }

    // Devuelve la dirección de la terminal de origen.
    fun obtenerTerminalOrigen(terminal: Terminal): String {
        return when (terminal) {
            Terminal.MISANTLA -> DIRECCION_TERMINAL_MISANTLA
            Terminal.MARTINEZ -> DIRECCION_TERMINAL_MARTINEZ
        }
    }
}

// Objeto de utilidad para gestionar los horarios de las corridas.
object HorariosRuta {

    // Genera la lista de horarios de salida, desde las 5:00 AM hasta las 9:00 PM, cada 20 minutos.
    fun generarHorarios(): List<String> {
        val horarios = mutableListOf<String>()

        val minutosInicio = 5 * 60  // 5:00 AM
        val minutosFin = 21 * 60     // 9:00 PM
        var minutosTotales = minutosInicio

        while (minutosTotales <= minutosFin) {
            val horas = minutosTotales / 60
            val minutos = minutosTotales % 60

            // Formato de 12 horas (AM/PM)
            val periodo = if (horas < 12) "AM" else "PM"
            val hora12 = when {
                horas == 0 -> 12
                horas > 12 -> horas - 12
                else -> horas
            }

            val horarioFormateado = String.format("%02d:%02d %s", hora12, minutos, periodo)
            horarios.add(horarioFormateado)

            minutosTotales += 20 // Siguiente corrida en 20 minutos
        }

        return horarios
    }

    //Obtiene el primer horario disponible como sugerencia
    fun obtenerHorarioSugerido(): String {
        // Si la lista está vacía, devuelve un valor por defecto.
        return generarHorarios().firstOrNull() ?: "05:00 AM"
    }

    // Verifica si un horario específico se encuentra en la lista de corridas válidas.
    fun esHorarioValido(horario: String): Boolean {
        return generarHorarios().contains(horario)
    }
}
