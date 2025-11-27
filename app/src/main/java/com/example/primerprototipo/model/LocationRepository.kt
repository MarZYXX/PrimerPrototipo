package com.example.primerprototipo.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.primerprototipo.model.UbicacionAutobus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationRepository {

    private const val BUS_LOCATIONS_NODE = "bus_locations"
    private val database = Firebase.database.reference

    private val _ubicaciones = MutableLiveData<Map<String, UbicacionAutobus>>()
    val ubicaciones: LiveData<Map<String, UbicacionAutobus>> = _ubicaciones

    private var valueEventListener: ValueEventListener? = null

    fun iniciarEscuchaUbicaciones(rutaFilter: String = "") {
        detenerEscucha() // Detener escuchas anteriores para no duplicar

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevasUbicaciones = mutableMapOf<String, UbicacionAutobus>()
                snapshot.children.forEach { child ->
                    val ubicacion = child.getValue(UbicacionAutobus::class.java)
                    if (ubicacion != null) {
                        if (rutaFilter.isEmpty() || ubicacion.ruta == rutaFilter) {
                            nuevasUbicaciones[child.key!!] = ubicacion
                        }
                    }
                }
                _ubicaciones.postValue(nuevasUbicaciones)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar error
            }
        }

        database.child(BUS_LOCATIONS_NODE).addValueEventListener(valueEventListener!!)
    }

    fun detenerEscucha() {
        valueEventListener?.let {
            database.child(BUS_LOCATIONS_NODE).removeEventListener(it)
        }
    }

    fun obtenerAutobusMasCercano(latitud: Double, longitud: Double, ruta: String): UbicacionAutobus? {
        val busesEnRuta = _ubicaciones.value?.values?.filter { it.ruta == ruta }
        return busesEnRuta?.minByOrNull { calcularDistancia(latitud, longitud, it.latitud, it.longitud) }
    }

    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371.0 // Radio de la Tierra en kilómetros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return radioTierra * c
    }
}
