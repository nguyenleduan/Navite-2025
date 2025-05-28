package com.nguyenleduan.app.fpl

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        Log.d("MyFirebaseMsgService", "Received data payload: $data")
        val type = data["type"]
        val apkUrl = data["apk"]

        Log.d("MyFirebaseMsgService", "Type: $type, ApkUrl: $apkUrl")
        if (type == "update" && apkUrl != null) {
            downloadApk(applicationContext, apkUrl)
        }
    }

    private fun downloadApk(context: Context, url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
        val fileName = "update.apk"
        request.setTitle("Đang tải bản cập nhật...")
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        val prefs = context.getSharedPreferences("apk_download", MODE_PRIVATE)
        prefs.edit().putLong("download_id", downloadId).apply()
    }
}