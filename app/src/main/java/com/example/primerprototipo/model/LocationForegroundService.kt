package com.example.primerprototipo.model

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var db: FirebaseFirestore

    private var choferId: String? = null
    private var autobusId: String? = null
    private var ruta: String? = null

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_CHANNEL_ID = "location_service_channel"
        private const val NOTIFICATION_ID = 101
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = FirebaseFirestore.getInstance()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        choferId = intent?.getStringExtra("CHOFER_ID")
        autobusId = intent?.getStringExtra("AUTOBUS_ID")
        ruta = intent?.getStringExtra("RUTA_ID")

        if (choferId == null || autobusId == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification("Rastreando ubicación...")
        startForeground(NOTIFICATION_ID, notification)

        startLocationUpdates()

        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 segundos
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return

                val ubicacion = UbicacionAutobus(
                    autobusId = autobusId!!,
                    choferId = choferId!!,
                    ruta = ruta ?: "",
                    latitud = location.latitude,
                    longitud = location.longitude,
                    velocidad = location.speed,
                    precision = location.accuracy,
                    timestamp = Date(location.time)
                )

                db.collection("ubicaciones_autobuses")
                    .document(autobusId!!)
                    .set(ubicacion)
                    .addOnSuccessListener {
                        Log.d(TAG, "Ubicación actualizada en Firebase")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error al actualizar ubicación: ${e.message}")
                    }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de seguridad al solicitar actualizaciones de ubicación: ${e.message}")
            stopSelf()
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Servicio de Ubicación")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono por defecto
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Canal de Servicio de Ubicación",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
