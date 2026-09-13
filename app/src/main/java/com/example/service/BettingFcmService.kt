package com.example.service

import android.util.Log
import com.example.data.model.BestPickNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BettingFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token: $token")
        FcmNotificationManager.saveToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notificationPayload = remoteMessage.notification

        val title = notificationPayload?.title
            ?: data["title"]
            ?: "🔥 Best Pick Prediction Alert"

        val body = notificationPayload?.body
            ?: data["body"]
            ?: data["message"]
            ?: "New high-confidence prediction available for today."

        val matchTitle = data["matchTitle"]
            ?: data["match"]
            ?: "Featured Match"

        val predictionTip = data["predictionTip"]
            ?: data["tip"]
            ?: data["prediction"]
            ?: "Best Value Outcome"

        val odds = data["odds"]?.toDoubleOrNull() ?: 1.85
        val confidence = data["confidence"]?.toIntOrNull() ?: 80
        val siteSource = data["siteSource"] ?: data["source"] ?: "Consensus Engine"
        val sportyBetCode = data["sportyBetCode"] ?: data["code"] ?: ""
        val scheduleDay = data["scheduleDay"] ?: "Today"
        val updateType = data["updateType"] ?: "NEW_PICK"

        val bestPickNotification = BestPickNotification(
            title = title,
            message = body,
            matchTitle = matchTitle,
            predictionTip = predictionTip,
            odds = odds,
            confidence = confidence,
            siteSource = siteSource,
            sportyBetCode = sportyBetCode,
            scheduleDay = scheduleDay,
            updateType = updateType,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        FcmNotificationManager.postBestPickNotification(applicationContext, bestPickNotification)
    }

    companion object {
        private const val TAG = "BettingFcmService"
    }
}
