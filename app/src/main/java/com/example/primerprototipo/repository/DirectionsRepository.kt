package com.example.primerprototipo.repository

import com.example.primerprototipo.model.Parada
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DirectionsRepository {

    // ⚠️====================================================================================⚠️
    // ⚠️ ¡ATENCIÓN! DEBES REEMPLAZAR ESTO CON TU CLAVE DE API DE GOOGLE MAPS REAL.         ⚠️
    // ⚠️ Y ASEGURARTE DE QUE LA "DIRECTIONS API" ESTÉ ACTIVADA EN TU GOOGLE CLOUD CONSOLE. ⚠️
    // ⚠️====================================================================================⚠️
    private const val API_KEY = "AIzaSyC0ij7ZfnTiTGCQtuOZeNK8QHAQU7qXz7I"

    suspend fun getDirections(paradas: List<Parada>): Result<List<LatLng>> {
        if (paradas.size < 2) return Result.failure(Exception("Se necesitan al menos 2 paradas."))

        return withContext(Dispatchers.IO) {
            try {
                val geoApiContext = GeoApiContext.Builder()
                    .apiKey(API_KEY)
                    .build()

                val origen = paradas.first()
                val destino = paradas.last()
                val waypoints = paradas.subList(1, paradas.size - 1).map { "${it.latitud},${it.longitud}" }.toTypedArray()

                val result = DirectionsApi.newRequest(geoApiContext)
                    .origin("${origen.latitud},${origen.longitud}")
                    .destination("${destino.latitud},${destino.longitud}")
                    .waypoints(*waypoints)
                    .mode(TravelMode.DRIVING)
                    .await()

                if (result.routes.isNotEmpty()) {
                    val decodedPath = result.routes[0].overviewPolyline.decodePath()
                    Result.success(decodedPath.map { LatLng(it.lat, it.lng) })
                } else {
                    Result.failure(Exception("No se encontraron rutas en la respuesta de la API."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e) // Devolver el error real
            }
        }
    }
}
