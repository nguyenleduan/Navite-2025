
package com.nguyenleduan.app.fpl
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.R


class MyForegroundService : Service() {
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onBind(intent: Intent?): IBinder? {
        return null  // Không bind
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Loading....")
        startForeground(1, notification)

        // TODO: Việc bạn cần làm ở đây (gửi location, chạy socket, v.v.)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startGettingLocation()
        return START_STICKY
    }

    private fun startGettingLocation() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(5000)
            .setWaitForAccurateLocation(true)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location: Location in result.locations) {
                    Log.d("GPS", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    val notification = createNotification("Lat: ${location.latitude}, Lng: ${location.longitude}")
                    startForeground(1, notification)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
    private fun createNotification(body: String): Notification {
        val channelId = "my_channel_id"
        val channelName = "My Background Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("App đang chạy nền")
            .setContentText("Location: ${body}" )
            .setSmallIcon(R.drawable.ic_dialog_info)

        return notificationBuilder.build()
    }
}
