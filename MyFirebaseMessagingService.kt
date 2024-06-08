package com.example.chucksgourmet

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Arrays
import java.util.Collections


class MyFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        const val CHANNEL_ID = "my_notification_channel"
        const val BASE_URL = "https://fcm.googleapis.com"
        const val FCM_SEND_ENDPOINT = "/v1/projects/chucksgourmet/messages:send"
        @JvmStatic
        fun sendNotificationToDevice(context: Context, title: String, body: String) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val json = """
                        {
                            "message": {
                                "token": "device token",
                                "notification": {
                                    "title": "$title",
                                    "body": "$body"
                                }
                            }
                        }
                    """.trimIndent()

                    val url = URL("$BASE_URL$FCM_SEND_ENDPOINT")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Authorization", "Bearer ${getAccessToken(context)}")
                    conn.setRequestProperty("Content-Type", "application/json; UTF-8")
                    conn.doOutput = true

                    val outputStream = conn.outputStream
                    outputStream.write(json.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = conn.responseCode
                    Log.d("FCM Send", "Response Code: $responseCode")
                } catch (e: Exception) {
                    val errorMessage = e.message ?: "Unknown error"
                    val exceptionType = e::class.simpleName ?: "Exception"
                    Log.e("FCM Send", "Error sending notification: $errorMessage ($exceptionType)")
                }
            }
        }

        @Throws(IOException::class)
        private fun getAccessToken(context: Context): String {
            val rawResource = context.resources.openRawResource(R.raw.service_account)
            val googleCredentials = GoogleCredentials.fromStream(rawResource)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refresh()
            return googleCredentials.accessToken.tokenValue
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle incoming messages here
        Log.d("MYTAG", "From: ${remoteMessage.from}")

        // Check if the message contains data
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("MYTAG", "Message data payload: ${remoteMessage.data}")
            // Handle data payload here
        }

        // Check if the message contains notification
        remoteMessage.notification?.let {
            Log.d("MYTAG", "Message Notification Body: ${it.body}")
            // Handle notification payload here
            sendNotification(it.title, it.body)
        }
    }

    private fun sendNotification(title: String?, body: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.chicken)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }


}



