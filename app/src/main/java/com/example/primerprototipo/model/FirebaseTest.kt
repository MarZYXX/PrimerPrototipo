package com.example.primerprototipo.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.primerprototipo.model.UbicacionAutobus
import java.util.Date

object FirebaseTest {
    private const val TAG = "FirebaseTest"
    private val db = FirebaseFirestore.getInstance()

    fun testFirebaseConnection() {
        val testUbicacion = UbicacionAutobus(
            autobusId = "test-bus-001",
            choferId = "test-chofer",
            ruta = "Misantla-Martinez",
            latitud = 19.9319,
            longitud = -96.8461,
            timestamp = Date()
        )

        db.collection("ubicaciones_autobuses")
            .document(testUbicacion.autobusId)
            .set(testUbicacion)
            .addOnSuccessListener {
                Log.d(TAG, " Firebase conectado correctamente")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error conectando a Firebase: ${e.message}")
            }
    }
}