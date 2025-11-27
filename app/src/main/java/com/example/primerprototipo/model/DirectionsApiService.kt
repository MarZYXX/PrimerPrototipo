package com.example.primerprototipo.service

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Servicio para obtener rutas reales de Google Directions API
 */
object DirectionsApiService {

    private const val TAG = "DirectionsAPI"
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"

    // ⚠️ IMPORTANTE: Reemplaza con tu API Key
    // Esta debe ser la MISMA API Key que usas en AndroidManifest
    private const val API_KEY = "AIzaSyC0ij7ZfnTiTGCQtuOZeNK8QHAQU7qXz7I"

    /**
     * Obtiene la ruta real entre origen y destino
     * @return Lista de LatLng que forman la ruta por carretera
     */
    suspend fun obtenerRutaReal(
        origen: LatLng,
        destino: LatLng,
        waypoints: List<LatLng> = emptyList()
    ): RutaResult = withContext(Dispatchers.IO) {
        try {
            val url = construirURL(origen, destino, waypoints)
            Log.d(TAG, "🌐 Solicitando ruta: $url")

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream))
                    .use { it.readText() }

                val result = parsearRespuesta(response)
                Log.d(TAG, "✅ Ruta obtenida: ${result.puntos.size} puntos")
                result
            } else {
                Log.e(TAG, "❌ Error HTTP: $responseCode")
                RutaResult.error("Error HTTP: $responseCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener ruta: ${e.message}")
            RutaResult.error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Construye la URL para la API de Directions
     */
    private fun construirURL(
        origen: LatLng,
        destino: LatLng,
        waypoints: List<LatLng>
    ): String {
        val origenStr = "${origen.latitude},${origen.longitude}"
        val destinoStr = "${destino.latitude},${destino.longitude}"

        var url = "$BASE_URL?origin=$origenStr&destination=$destinoStr"

        // Agregar paradas intermedias (waypoints) si existen
        if (waypoints.isNotEmpty()) {
            val waypointsStr = waypoints.joinToString("|") {
                "${it.latitude},${it.longitude}"
            }
            url += "&waypoints=$waypointsStr"
        }

        // Parámetros adicionales
        url += "&mode=driving" // Modo conducción
        url += "&language=es" // Idioma español
        url += "&key=$API_KEY"

        return url
    }

    /**
     * Parsea la respuesta JSON de la API
     */
    private fun parsearRespuesta(jsonStr: String): RutaResult {
        try {
            val json = JSONObject(jsonStr)
            val status = json.getString("status")

            if (status != "OK") {
                return RutaResult.error("Status: $status")
            }

            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                return RutaResult.error("No se encontraron rutas")
            }

            val route = routes.getJSONObject(0)
            val overviewPolyline = route.getJSONObject("overview_polyline")
            val encodedPoints = overviewPolyline.getString("points")

            // Decodificar los puntos de la polilínea
            val puntos = decodificarPolyline(encodedPoints)

            // Obtener distancia y duración
            val legs = route.getJSONArray("legs")
            var distanciaTotal = 0
            var duracionTotal = 0

            for (i in 0 until legs.length()) {
                val leg = legs.getJSONObject(i)
                distanciaTotal += leg.getJSONObject("distance").getInt("value")
                duracionTotal += leg.getJSONObject("duration").getInt("value")
            }

            return RutaResult(
                puntos = puntos,
                distanciaMetros = distanciaTotal,
                duracionSegundos = duracionTotal,
                exitosa = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al parsear respuesta: ${e.message}")
            return RutaResult.error("Error al parsear: ${e.message}")
        }
    }

    /**
     * Decodifica una polilínea codificada de Google Maps
     * Algoritmo de Google: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     */
    private fun decodificarPolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(latLng)
        }

        return poly
    }

    /**
     * Resultado de una consulta de ruta
     */
    data class RutaResult(
        val puntos: List<LatLng> = emptyList(),
        val distanciaMetros: Int = 0,
        val duracionSegundos: Int = 0,
        val exitosa: Boolean = false,
        val error: String? = null
    ) {
        companion object {
            fun error(mensaje: String?) = RutaResult(
                exitosa = false,
                error = mensaje
            )
        }

        fun obtenerDistanciaKm(): Double = distanciaMetros / 1000.0

        fun obtenerDuracionMinutos(): Int = duracionSegundos / 60

        fun obtenerDuracionFormateada(): String {
            val horas = duracionSegundos / 3600
            val minutos = (duracionSegundos % 3600) / 60
            return if (horas > 0) {
                "${horas}h ${minutos}min"
            } else {
                "${minutos}min"
            }
        }
    }
}