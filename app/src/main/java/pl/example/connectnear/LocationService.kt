package pl.example.connectnear

import android.app.Notification
import android.app.NotificationChannel
//import android.app.Notificationimport android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*

class LocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Interwał aktualizacji w tle (np. 30 sekund)
    private val UPDATE_INTERVAL = 30000L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForegroundService()
        startLocationUpdates()
    }

    // --- TO JEST NOWOŚĆ: Wykrywanie zamknięcia aplikacji ---
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // Gdy użytkownik "ubije" aplikację, usuwamy lokalizację z bazy
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("location", null) // Kasujemy współrzędne
        }

        // Zatrzymujemy usługę
        stopSelf()
    }
    // -------------------------------------------------------

    private fun startForegroundService() {
        val channelId = "location_channel"
        val channelName = "Lokalizacja w tle"

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("ConnectNear")
            .setContentText("Udostępnianie lokalizacji w tle...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        serviceScope.launch {
            while (isActive) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    try {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener { location ->
                                if (location != null) {
                                    val geoPoint = GeoPoint(location.latitude, location.longitude)

                                    FirebaseFirestore.getInstance().collection("users")
                                        .document(userId)
                                        .update("location", geoPoint)
                                }
                            }
                    } catch (e: SecurityException) { }
                }
                delay(UPDATE_INTERVAL)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        // Opcjonalnie: Tu też można wywołać kasowanie, ale onTaskRemoved jest pewniejsze przy "swipe"
    }
}
