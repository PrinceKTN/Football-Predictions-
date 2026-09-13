package com.example.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.BestPickNotification
import com.example.data.model.SportyBetPick
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FcmNotificationManager {
    private const val TAG = "FcmNotificationManager"
    const val CHANNEL_ID = "best_picks_channel"
    const val TOPIC_BEST_PICKS = "best_picks"
    private const val PREFS_NAME = "fcm_best_picks_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    const val EXTRA_NAVIGATE_TO = "extra_navigate_to"
    const val DESTINATION_TESTING = "TESTING"
    const val EXTRA_PICK_ID = "extra_pick_id"

    private val _fcmTokenFlow = MutableStateFlow("")
    val fcmTokenFlow = _fcmTokenFlow.asStateFlow()

    private val _notificationsEnabledFlow = MutableStateFlow(true)
    val notificationsEnabledFlow = _notificationsEnabledFlow.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        val savedToken = prefs.getString(KEY_FCM_TOKEN, "") ?: ""
        
        _notificationsEnabledFlow.value = isEnabled
        _fcmTokenFlow.value = savedToken

        createNotificationChannel(context)

        // Fetch FCM token
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "FCM Registration Token: $token")
                    saveToken(context, token)
                    if (isEnabled) {
                        subscribeToBestPicksTopic(context)
                    }
                } else {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization note: ${e.message}")
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Best Pick Predictions & Updates"
            val descriptionText = "Real-time alerts when new high-confidence Best Picks and daily odds updates are posted."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = 0xFF00E5FF.toInt() // Cyan Accent
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun saveToken(context: Context, token: String) {
        _fcmTokenFlow.value = token
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FCM_TOKEN, token)
            .apply()
    }

    fun getStoredToken(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FCM_TOKEN, "") ?: ""
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean, onDone: (Boolean) -> Unit = {}) {
        _notificationsEnabledFlow.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()

        try {
            if (enabled) {
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_BEST_PICKS)
                    .addOnCompleteListener { task ->
                        Log.d(TAG, "Subscribed to topic $TOPIC_BEST_PICKS: ${task.isSuccessful}")
                        onDone(task.isSuccessful)
                    }
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_BEST_PICKS)
                    .addOnCompleteListener { task ->
                        Log.d(TAG, "Unsubscribed from topic $TOPIC_BEST_PICKS: ${task.isSuccessful}")
                        onDone(task.isSuccessful)
                    }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Topic subscription error: ${e.message}")
            onDone(false)
        }
    }

    private fun subscribeToBestPicksTopic(context: Context) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_BEST_PICKS)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully registered to $TOPIC_BEST_PICKS FCM topic")
                }
        } catch (e: Exception) {
            Log.w(TAG, "Subscribe topic error: ${e.message}")
        }
    }

    fun postBestPickNotification(context: Context, notification: BestPickNotification) {
        // Check notification permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted, persisting in database only")
                persistNotificationInDb(context, notification)
                return
            }
        }

        // Tap PendingIntent - open app directly to Testing/Picks tab
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, DESTINATION_TESTING)
            putExtra(EXTRA_PICK_ID, notification.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.toInt().takeIf { it != 0 } ?: (System.currentTimeMillis() % 10000).toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val bigText = buildString {
            append("⚽ Match: ${notification.matchTitle}\n")
            append("🎯 Tip: ${notification.predictionTip} (Odds: ${notification.odds})\n")
            append("📊 Confidence: ${notification.confidence}% | Source: ${notification.siteSource}\n")
            if (notification.sportyBetCode.isNotEmpty()) {
                append("🎟️ SportyBet Booking Code: ${notification.sportyBetCode}")
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(notification.title)
                    .setSummaryText("${notification.scheduleDay} Best Pick • ${notification.confidence}% Conf.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setColor(0xFF00E5FF.toInt())
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)
            .addAction(
                R.mipmap.ic_launcher,
                "View Best Picks",
                pendingIntent
            )

        val notificationManager = NotificationManagerCompat.from(context)
        val notifId = (notification.id.takeIf { it != 0L } ?: System.currentTimeMillis()).toInt()
        try {
            notificationManager.notify(notifId, builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while notifying: ${e.message}")
        }

        // Persist to Room
        persistNotificationInDb(context, notification)
    }

    private fun persistNotificationInDb(context: Context, notification: BestPickNotification) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.bestPickNotificationDao().insertNotification(notification)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist notification in DB: ${e.message}")
            }
        }
    }

    fun simulateIncomingBestPickAlert(
        context: Context,
        pick: SportyBetPick? = null,
        isUpdate: Boolean = false
    ) {
        val p = pick ?: SportyBetPick(
            id = "fcm_test_${System.currentTimeMillis()}",
            match = "Arsenal vs Chelsea",
            homeTeam = "Arsenal",
            awayTeam = "Chelsea",
            league = "Premier League",
            country = "England",
            scheduleDay = "Today",
            kickOffTime = "19:30",
            tip = "Home Win & Over 1.5",
            estimatedOdds = 1.85,
            sportyBetMarketName = "1X2 & Over/Under 1.5",
            sportyBetSelection = "1 & Over 1.5",
            algorithmicSource = "Forebet + PredictZ consensus",
            confidenceLevel = "82% Confidence",
            stepByStepSportyBetGuide = "Search Arsenal -> Match Result & Over/Under -> Select 1 & Over 1.5"
        )

        val conf = p.confidenceLevel.filter { it.isDigit() }.toIntOrNull() ?: 80

        val title = if (isUpdate) {
            "⚡ Best Pick Odds Updated: ${p.match}"
        } else {
            "🔥 New Best Pick Posted: ${p.match}"
        }

        val message = if (isUpdate) {
            "Odds updated to ${p.estimatedOdds} (${p.confidenceLevel}) for ${p.tip}"
        } else {
            "Tip: ${p.tip} @ ${p.estimatedOdds} (${p.confidenceLevel}) via ${p.algorithmicSource}"
        }

        val notification = BestPickNotification(
            title = title,
            message = message,
            matchTitle = p.match,
            predictionTip = p.tip,
            odds = p.estimatedOdds,
            confidence = conf,
            siteSource = p.algorithmicSource,
            sportyBetCode = "SB-8821",
            scheduleDay = p.scheduleDay,
            updateType = if (isUpdate) "ODDS_UPDATE" else "NEW_PICK",
            timestamp = System.currentTimeMillis()
        )

        postBestPickNotification(context, notification)
    }
}
