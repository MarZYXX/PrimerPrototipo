package com.example.primerprototipo.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.primerprototipo.model.Autobus
import com.example.primerprototipo.model.UbicacionAutobus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseManager {

    private const val BUS_LOCATIONS_NODE = "bus_locations"
    private val database = Firebase.database.reference.child(BUS_LOCATIONS_NODE)

    private val _autobusesEnRuta = MutableLiveData<List<UbicacionAutobus>>()
    val autobusesEnRuta: LiveData<List<UbicacionAutobus>> = _autobusesEnRuta

    private var listener: ValueEventListener? = null

    fun updateBusLocation(autobus: Autobus) {
        val busLocation = UbicacionAutobus(
            autobusId = autobus.numeroUnidad,
            choferId = autobus.id,
            ruta = autobus.ruta,
            latitud = autobus.latitud,
            longitud = autobus.longitud,
            proximaParada = autobus.proximaParada,
            pasajerosAbordo = autobus.pasajerosAbordo
        )
        database.child(autobus.numeroUnidad).setValue(busLocation)
    }

    fun escucharAutobusesPorRuta(nombreRuta: String) {
        dejarDeEscucharAutobuses()
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val autobuses = snapshot.children.mapNotNull { it.getValue(UbicacionAutobus::class.java) }
                    .filter { it.ruta == nombreRuta }
                _autobusesEnRuta.postValue(autobuses)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.addValueEventListener(listener!!)
    }

    fun dejarDeEscucharAutobuses() {
        listener?.let { database.removeEventListener(it) }
    }
}