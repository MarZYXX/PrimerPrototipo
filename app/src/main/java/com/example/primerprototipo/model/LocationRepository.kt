package com.example.primerprototipo.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.primerprototipo.model.UbicacionAutobus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object LocationRepository {

    private val db = FirebaseFirestore.getInstance()
    private val _ubicaciones = MutableLiveData<Map<String, UbicacionAutobus>>()
    val ubicaciones: LiveData<Map<String, UbicacionAutobus>> = _ubicaciones

    private var listener: ListenerRegistration? = null

    fun iniciarEscuchaUbicaciones(rutaFiltro: String? = null) {
        listener?.remove()

        val collection = db.collection("ubicaciones_autobuses")

        listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Error escuchando ubicaciones: ${error.message}")
                return@addSnapshotListener
            }

            snapshot?.let { querySnapshot ->
                val nuevasUbicaciones = mutableMapOf<String, UbicacionAutobus>()

                querySnapshot.documents.forEach { document ->
                    val ubicacion = document.toObject(UbicacionAutobus::class.java)
                    ubicacion?.let {
                        // Filtrar por ruta si se especifica
                        if (rutaFiltro == null || it.ruta == rutaFiltro) {
                            nuevasUbicaciones[it.autobusId] = it
                        }
                    }
                }

                _ubicaciones.value = nuevasUbicaciones
            }
        }
    }

    fun detenerEscucha() {
        listener?.remove()
        listener = null
    }

    fun obtenerAutobusMasCercano(
        userLat: Double,
        userLng: Double,
        ruta: String
    ): UbicacionAutobus? {
        val ubicacionesActuales = _ubicaciones.value ?: return null

        return ubicacionesActuales.values
            .filter { it.ruta == ruta && it.enRuta }
            .minByOrNull { calcularDistancia(userLat, userLng, it.latitud, it.longitud) }
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}