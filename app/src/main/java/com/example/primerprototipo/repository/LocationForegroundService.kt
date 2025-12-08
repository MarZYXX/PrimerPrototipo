package com.example.primerprototipo.repository

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.primerprototipo.model.UbicacionAutobus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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

    // Crea el servicio en primer plano y lo inicializa
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        db = FirebaseFirestore.getInstance()
        createNotificationChannel()
        Log.d(TAG, "Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        choferId = intent?.getStringExtra("CHOFER_ID")
        autobusId = intent?.getStringExtra("AUTOBUS_ID")
        ruta = intent?.getStringExtra("RUTA_ID")

        Log.d(TAG, "Datos recibidos - Chofer: $choferId, Bus: $autobusId, Ruta: $ruta")

        if (choferId == null || autobusId == null) {
            Log.e(TAG, "Faltan datos requeridos. Deteniendo servicio")
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = createNotification("Rastreando ubicación del autobús $autobusId...")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(TAG, "Servicio iniciado en primer plano")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar servicio en primer plano: ${e.message}")
        }

        startLocationUpdates()

        return START_STICKY
    }

    // Inicia las actualizaciones de ubicación en segundo plano y las guarda en Firestore si es necesario
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 segundos
        ).apply {
            setMinUpdateIntervalMillis(5000L) // 5 segundos mínimo
            setWaitForAccurateLocation(false)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return

                Log.d(TAG, "Nueva ubicación: ${location.latitude}, ${location.longitude}")

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

                // Guardar en Firestore
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
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Actualizaciones de ubicación iniciadas")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos: ${e.message}")
            stopSelf()
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("GPS-BUS - Rastreo Activo")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Servicio de Ubicación GPS-BUS",
                NotificationManager.IMPORTANCE_LOW // LOW para que no moleste
            ).apply {
                description = "Rastrea la ubicación del autobús en tiempo real"
            }

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Servicio detenido")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}