package com.example.primerprototipo.model

data class Parada(
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val orden: Int
)

enum class Terminal(val nombreCompleto: String) {
    MISANTLA("Terminal Misantla"),
    MARTINEZ("Terminal Martínez de la Torre");

    override fun toString(): String = nombreCompleto
}

object RutasMisantla {

    // Ruta Misantla -> Martínez (PARADAS REALES Y COMPLETAS)
    val paradasMisantlaMartinez = listOf(
        Parada("Terminal Misantla", 19.9319, -96.8461, 1),
        Parada("Santa Cruz", 19.9350, -96.8400, 2),
        Parada("Desviación", 19.9380, -96.8350, 3),
        Parada("Primavera", 19.9420, -96.8320, 4),
        Parada("Arroyo Hondo", 19.9450, -96.8300, 5),
        Parada("San Francisco", 19.9500, -96.8250, 6),
        Parada("Santa Clara", 19.9550, -96.8200, 7),
        Parada("Coapeche", 19.9800, -96.7900, 8),
        Parada("Palpuala Ixcan", 20.0100, -96.7600, 9),
        Parada("Libertad", 20.0300, -96.7400, 10),
        Parada("Plan de Limón", 20.0450, -96.7200, 11),
        Parada("Independencia", 20.0600, -96.7000, 12),
        Parada("Terminal Martínez de la Torre", 20.0667, -97.0667, 13)
    )

    // Ruta Martínez -> Misantla (ORDEN INVERSO)
    val paradasMartinezMisantla = listOf(
        Parada("Terminal Martínez de la Torre", 20.0667, -97.0667, 1),
        Parada("Independencia", 20.0600, -96.7000, 2),
        Parada("Plan de Limón", 20.0450, -96.7200, 3),
        Parada("Libertad", 20.0300, -96.7400, 4),
        Parada("Palpuala Ixcan", 20.0100, -96.7600, 5),
        Parada("Coapeche", 19.9800, -96.7900, 6),
        Parada("Santa Clara", 19.9550, -96.8200, 7),
        Parada("San Francisco", 19.9500, -96.8250, 8),
        Parada("Arroyo Hondo", 19.9450, -96.8300, 9),
        Parada("Primavera", 19.9420, -96.8320, 10),
        Parada("Desviación", 19.9380, -96.8350, 11),
        Parada("Santa Cruz", 19.9350, -96.8400, 12),
        Parada("Terminal Misantla", 19.9319, -96.8461, 13)
    )

    /**
     * Obtiene las paradas según la terminal de salida seleccionada
     */
    fun obtenerParadasPorTerminal(terminal: Terminal): List<Parada> {
        return when (terminal) {
            Terminal.MISANTLA -> paradasMisantlaMartinez
            Terminal.MARTINEZ -> paradasMartinezMisantla
        }
    }

    /**
     * Obtiene el nombre de la ruta según la terminal de salida
     */
    fun obtenerNombreRuta(terminal: Terminal): String {
        return when (terminal) {
            Terminal.MISANTLA -> "Misantla → Martínez"
            Terminal.MARTINEZ -> "Martínez → Misantla"
        }
    }

    fun obtenerParadasPorRuta(nombreRuta: String): List<Parada> {
        return when (nombreRuta) {
            "Misantla - Martinez de la Torre" -> paradasMisantlaMartinez
            "Martinez de la Torre - Misantla" -> paradasMartinezMisantla
            else -> emptyList()
        }
    }

    /**
     * Obtiene la terminal de destino según la terminal de salida
     */
    fun obtenerTerminalDestino(terminal: Terminal): String {
        return when (terminal) {
            Terminal.MISANTLA -> "Martínez de la Torre"
            Terminal.MARTINEZ -> "Misantla"
        }
    }
}

object HorariosRuta {

    /**
     * Genera los horarios de salida cada 20 minutos
     * Primera corrida: 05:00 AM
     * Última corrida: 21:00 PM (9:00 PM)
     */
    fun generarHorarios(): List<String> {
        val horarios = mutableListOf<String>()

        // Hora de inicio: 5:00 AM (300 minutos desde medianoche)
        // Hora de fin: 21:00 PM (1260 minutos desde medianoche)
        var minutosTotales = 5 * 60 // 5:00 AM
        val minutosFin = 21 * 60    // 9:00 PM

        while (minutosTotales <= minutosFin) {
            val horas = minutosTotales / 60
            val minutos = minutosTotales % 60

            // Formato 12 horas con AM/PM
            val periodo = if (horas < 12) "AM" else "PM"
            val hora12 = when {
                horas == 0 -> 12
                horas > 12 -> horas - 12
                else -> horas
            }

            val horarioFormateado = String.format("%02d:%02d %s", hora12, minutos, periodo)
            horarios.add(horarioFormateado)

            // Incrementar 20 minutos
            minutosTotales += 20
        }

        return horarios
    }

    /**
     * Obtiene el horario más cercano actual o siguiente
     */
    fun obtenerHorarioSugerido(): String {
        val horarios = generarHorarios()
        // Por ahora retorna el primer horario disponible
        // En producción, calcularías basado en la hora actual
        return horarios.firstOrNull() ?: "05:00 AM"
    }

    /**
     * Valida si un horario está en la lista de horarios válidos
     */
    fun esHorarioValido(horario: String): Boolean {
        return generarHorarios().contains(horario)
    }
}