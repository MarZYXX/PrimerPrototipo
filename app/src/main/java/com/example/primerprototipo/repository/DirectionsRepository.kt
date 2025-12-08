package com.example.primerprototipo.repository

import com.example.primerprototipo.BuildConfig
import com.example.primerprototipo.model.Parada
import com.example.primerprototipo.model.Ruta
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DirectionsRepository {

    suspend fun getDirections(origen: String, destino: String): Result<Ruta> {
        return withContext(Dispatchers.IO) {
            try {
                val geoApiContext = GeoApiContext.Builder()
                    .apiKey(BuildConfig.MAPS_API_KEY)
                    .build()

                // Realizar la solicitud de direcciones a la API de Google Maps.
                val result = DirectionsApi.newRequest(geoApiContext)
                    .origin(origen)
                    .destination(destino)
                    .mode(TravelMode.DRIVING)
                    .await()

                if (result.routes.isNotEmpty()) {
                    val route = result.routes[0]
                    val leg = route.legs[0]

                    // Extraer la polilínea para dibujar la ruta completa.
                    val decodedPath = route.overviewPolyline.decodePath().map { LatLng(it.lat, it.lng) }

                    // Extraer las "paradas" de los pasos intermedios de la ruta.
                    val paradas = leg.steps.mapNotNull { step ->
                        // Usamos una expresión regular para limpiar las instrucciones HTML y obtener un nombre de lugar más limpio.
                        val nombreParada = step.htmlInstructions.replace(Regex("<[^>]*>"), " ").trim()
                        Parada(nombreParada, step.startLocation.lat, step.startLocation.lng)
                    }.distinctBy { it.latitud to it.longitud } // Eliminar duplicados si los hubiera.

                    // Añadir el destino final como la última parada.
                    val paradasConDestino = paradas + Parada("Destino: ${destino}", leg.endLocation.lat, leg.endLocation.lng)

                    // Devolver el objeto Ruta con la polilínea y las paradas dinámicas.
                    Result.success(Ruta(decodedPath, paradasConDestino))
                } else {
                    Result.failure(Exception("No se encontraron rutas en la respuesta de la API."))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }
}