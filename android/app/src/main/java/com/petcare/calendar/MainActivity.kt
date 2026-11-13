package com.petcare.calendar

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.petcare.calendar.core.di.ServiceLocator
import com.petcare.calendar.core.theme.AppTheme
import com.petcare.calendar.presentation.app.AppRoot

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        ServiceLocator.services.platform.reportPermissionResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        ServiceLocator.services.platform.apply {
            requestNotifications = {
                if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else reportPermissionResult()
            }
            openSettings = { startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)) }
            intent.getStringExtra("care_destination")?.let(::setDestination)
        }
        setContent { AppTheme { AppRoot() } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ServiceLocator.services.platform.setDestination(intent.getStringExtra("care_destination"))
    }

    override fun onDestroy() {
        ServiceLocator.services.platform.requestNotifications = null
        ServiceLocator.services.platform.openSettings = null
        super.onDestroy()
    }
}
