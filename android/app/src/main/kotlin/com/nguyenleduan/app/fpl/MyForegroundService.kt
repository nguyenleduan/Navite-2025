
package com.nguyenleduan.app.fpl

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.location.Location
import android.media.ImageReader
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

import android.R

class MyForegroundService : Service() {
    private var isLocationUpdating = false

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
        if (isLocationUpdating) return
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
//                    captureImageFromFrontCamera()
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationUpdating = true
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


    @SuppressLint("MissingPermission") // Bạn vẫn cần đảm bảo xin runtime permission bên ngoài
    private fun captureImageFromFrontCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val cameraDirection = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null && cameraDirection == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        Log.e("Camera", "CAMERA permission not granted")
                        // TODO: Yêu cầu quyền CAMERA tại runtime
                        return
                    }

                    cameraManager.openCamera(cameraId, object : android.hardware.camera2.CameraDevice.StateCallback() {
                        override fun onOpened(camera: android.hardware.camera2.CameraDevice) {
                            Log.d("Camera", "Camera opened")

                            val imageReader = android.media.ImageReader.newInstance(640, 480, android.graphics.ImageFormat.JPEG, 1)
                            val captureRequestBuilder = camera.createCaptureRequest(android.hardware.camera2.CameraDevice.TEMPLATE_STILL_CAPTURE)
                            captureRequestBuilder.addTarget(imageReader.surface)

                            imageReader.setOnImageAvailableListener({ reader ->
                                val image = reader.acquireLatestImage()
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                image.close()

                                // Lưu ảnh vào thư mục Download bằng MediaStore
                                val resolver = applicationContext.contentResolver
                                val contentValues = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, "front_camera_${System.currentTimeMillis()}.jpg")
                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                                }

                                val imageUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                                if (imageUri != null) {
                                    resolver.openOutputStream(imageUri).use { outputStream ->
                                        outputStream?.write(bytes)
                                        Log.d("Camera", "Saved image to Download folder: $imageUri")
                                    }
                                } else {
                                    Log.e("Camera", "Failed to create MediaStore record")
                                }

                                camera.close()
                                imageReader.close()
                            }, null)

                            camera.createCaptureSession(listOf(imageReader.surface), object : android.hardware.camera2.CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: android.hardware.camera2.CameraCaptureSession) {
                                    session.capture(captureRequestBuilder.build(), null, null)
                                }

                                override fun onConfigureFailed(session: android.hardware.camera2.CameraCaptureSession) {
                                    Log.e("Camera", "Camera configuration failed")
                                    camera.close()
                                    imageReader.close()
                                }
                            }, null)
                        }

                        override fun onDisconnected(camera: android.hardware.camera2.CameraDevice) {
                            Log.e("Camera", "Camera disconnected")
                            camera.close()
                        }

                        override fun onError(camera: android.hardware.camera2.CameraDevice, error: Int) {
                            Log.e("Camera", "Camera error: $error")
                            camera.close()
                        }
                    }, null)

                    break
                }
            }
        } catch (e: Exception) {
            Log.e("Camera", "Exception: ${e.message}")
        }
    }
}
