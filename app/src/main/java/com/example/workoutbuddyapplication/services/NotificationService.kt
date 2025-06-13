package com.example.workoutbuddyapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.workoutbuddyapplication.R
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.example.workoutbuddyapplication.screens.dataStore
import android.app.PendingIntent
import android.content.Intent
import androidx.core.net.toUri

object NotificationService {
    private const val CHANNEL_ID = "workout_channel"
    private const val CHANNEL_NAME = "Workout Notifications"
    private const val CHANNEL_DESC = "Notifications related to workouts"
    private const val NOTIFICATION_ID = 1

    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    private val GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("goal_reminder_notifications_enabled")
    private val WORKOUT_TIME_NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("workout_time_notification_enabled")

    fun areNotificationsEnabled(context: Context): Boolean {
        return runBlocking {
            context.dataStore.data.first()[NOTIFICATIONS_ENABLED_KEY] ?: true
        }
    }

    fun areGoalReminderNotificationsEnabled(context: Context): Boolean {
        return runBlocking {
            context.dataStore.data.first()[GOAL_REMINDER_NOTIFICATIONS_ENABLED_KEY] ?: false
        }
    }

    fun isWorkoutTimeNotificationEnabled(context: Context): Boolean {
        return runBlocking {
            context.dataStore.data.first()[WORKOUT_TIME_NOTIFICATION_ENABLED_KEY] ?: true
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendTestNotification(context: Context) {
        if (!areNotificationsEnabled(context)) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "POST_NOTIFICATIONS permission not granted.")
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Aktiv")
            .setContentText("This is a test notification")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    fun sendGoalDeadlineNotification(context: Context, goalId: Int, goalTitle: String) {
        if (!areNotificationsEnabled(context)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "POST_NOTIFICATIONS permission not granted.")
                return
            }
        }

        val intent = Intent(
            Intent.ACTION_VIEW,
            "workoutbuddy://goal/$goalId".toUri(),
            context,
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.component?.className?.let { Class.forName(it) }
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            goalId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Je hebt nog niet alle workout doelen behaald!")
            .setContentText("Je hebt nog minder dan 24 uur om $goalTitle te halen")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Je hebt nog minder dan 24 uur om $goalTitle te halen"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(1000 + goalId, builder.build())
        }
    }

    fun sendWorkoutTimeNotification(context: Context, contentTitle: String, contentText: String) {
        if (!areNotificationsEnabled(context) || !isWorkoutTimeNotificationEnabled(context)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "POST_NOTIFICATIONS permission not granted.")
                return
            }
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(2001, builder.build())
        }
    }
}