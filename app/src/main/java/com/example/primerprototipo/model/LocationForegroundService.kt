package com.example.primerprototipo.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.primerprototipo.R
import com.example.primerprototipo.model.UbicacionAutobus
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LocationForegroundService : Service() {

    private val binder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var currentAutobusId: String = ""
    private var currentRuta: String = ""
    private var currentChoferId: String = ""

    // Firebase
    private val db = FirebaseFirestore.getInstance()

    inner class LocalBinder : Binder() {
        fun getService(): LocationForegroundService = this@LocationForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationRequest()
        createNotificationChannel()
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 segundos
            fastestInterval = 5000 // 5 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 10f // 10 metros mínimo para actualizar
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    enviarUbicacionAlServidor(location)
                }
            }
        }
    }

    fun iniciarRastreo(autobusId: String, ruta: String, choferId: String) {
        this.currentAutobusId = autobusId
        this.currentRuta = ruta
        this.currentChoferId = choferId

        // Iniciar notificación foreground
        startForeground(1, createNotification())

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun detenerRastreo() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun enviarUbicacionAlServidor(location: Location) {
        val ubicacion = UbicacionAutobus(
            autobusId = currentAutobusId,
            choferId = currentChoferId,
            ruta = currentRuta,
            latitud = location.latitude,
            longitud = location.longitude,
            velocidad = location.speed,
            precision = location.accuracy,
            timestamp = Date()
        )

        // Guardar en Firebase Firestore
        db.collection("ubicaciones_autobuses")
            .document(currentAutobusId)
            .set(ubicacion)
            .addOnSuccessListener {
                println("Ubicación enviada: $ubicacion")
            }
            .addOnFailureListener { e ->
                println("Error enviando ubicación: ${e.message}")
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "Seguimiento de Ubicación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Seguimiento GPS del autobús en tiempo real"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("GPS-BUS: Transmitiendo ubicación")
            .setContentText("Ruta: $currentRuta - Autobús: $currentAutobusId")
            .setSmallIcon(R.drawable.ic_bus)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent): IBinder = binder
}