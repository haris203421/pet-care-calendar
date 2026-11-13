package com.petcare.calendar.data.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.petcare.calendar.MainActivity
import com.petcare.calendar.R
import com.petcare.calendar.data.local.LocalPreferencesStore
import com.petcare.calendar.domain.model.CalendarData
import com.petcare.calendar.domain.usecase.CalculateNextReminderUseCase

class LocalReminderScheduler(private val context: Context, private val store: LocalPreferencesStore) {
    private val calculator = CalculateNextReminderUseCase()
    private val notifications = context.getSystemService(NotificationManager::class.java)
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private val alarmIntent: PendingIntent
        get() = PendingIntent.getBroadcast(context, 100, Intent(context, CareReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun allowed(): Boolean = notifications.areNotificationsEnabled() && (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)

    fun reconcile(data: CalendarData, deliver: Boolean = false) {
        alarms.cancel(alarmIntent)
        val validIds = calculator(data.copy(delivered = emptySet()), includeDue = true).map { it.id.hashCode() }.toSet()
        notifications.activeNotifications.filter { it.id !in validIds }.forEach { notifications.cancel(it.id) }
        if (!data.settings.reminders || data.settings.sampleMode || !allowed()) return
        notifications.createNotificationChannel(NotificationChannel("pet_care", context.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT))
        val now = System.currentTimeMillis()
        val candidates = calculator(data, now, includeDue = deliver)
        val due = if (deliver) candidates.filter { it.at <= now } else emptyList()
        if (due.isNotEmpty()) {
            store.update({ data }) { current -> current.copy(delivered = current.delivered + due.map { it.id }) }
            due.forEach { item ->
                val open = Intent(context, MainActivity::class.java).putExtra("care_destination", item.id).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val tap = PendingIntent.getActivity(context, item.id.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val notification = Notification.Builder(context, "pet_care")
                    .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                    .setContentTitle(context.getString(R.string.reminder_title, item.petName, item.title))
                    .setContentText(item.detail).setAutoCancel(true).setContentIntent(tap).build()
                notifications.notify(item.id.hashCode(), notification)
            }
        }
        val delivered = due.map { it.id }.toSet()
        candidates.firstOrNull { it.id !in delivered && it.at > now }?.let {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it.at, alarmIntent)
        }
    }
}
