package com.petcare.calendar.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.petcare.calendar.data.local.LocalDataSerializer
import com.petcare.calendar.data.local.LocalPreferencesStore

class ReminderRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        ReminderWork.enqueue(pending) {
            val store = LocalPreferencesStore(context.applicationContext, LocalDataSerializer())
            store.read()?.let { LocalReminderScheduler(context.applicationContext, store).reconcile(it) }
        }
    }
}
