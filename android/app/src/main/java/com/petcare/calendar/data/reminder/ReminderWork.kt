package com.petcare.calendar.data.reminder

import android.content.BroadcastReceiver
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ReminderWork {
    private val executor = ThreadPoolExecutor(1, 1, 30L, TimeUnit.SECONDS, ArrayBlockingQueue(16)).apply { allowCoreThreadTimeOut(true) }

    fun enqueue(pending: BroadcastReceiver.PendingResult, work: () -> Unit) {
        try {
            executor.execute {
                try { work() } catch (_: Exception) { Unit } finally { pending.finish() }
            }
        } catch (_: java.util.concurrent.RejectedExecutionException) {
            pending.finish()
        }
    }
}
