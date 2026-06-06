package com.example.exp.domain.permission

import android.Manifest
import android.util.Log
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.exp.domain.permission.PermissionChecker

/**
 * Manages all permission-related operations
 */
class PermissionManager(private val activity: ComponentActivity) {

    private val context: Context = activity
    val permissionChecker = PermissionChecker(context)
    
    private var permissionCallback: ((String, Boolean) -> Unit)? = null

    // Only keep launchers for permissions we actually use
    private val storagePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        Log.d("PermissionManager", "storage launcher result: $permissions")
        permissionCallback?.invoke("Storage", allGranted)
    }

    private val contactsPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("PermissionManager", "contacts launcher result: $isGranted")
        permissionCallback?.invoke("Contacts", isGranted)
    }

    private val microphonePermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("PermissionManager", "microphone launcher result: $isGranted")
        permissionCallback?.invoke("Microphone", isGranted)
    }

    private val smsPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        Log.d("PermissionManager", "sms launcher result: $permissions")
        permissionCallback?.invoke("SMS", allGranted)
    }

    private val notificationPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("PermissionManager", "notification launcher result: $isGranted")
        permissionCallback?.invoke("Enable Notification", isGranted)
    }

    /**
     * Request permission by name
     */
    fun requestPermission(permissionName: String, callback: (String, Boolean) -> Unit) {
        permissionCallback = callback
        Log.d("PermissionManager", "requestPermission called for: $permissionName")

        when (permissionName) {
            "Storage" -> {
                Log.d("PermissionManager", "launching Storage permissions")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storagePermissionLauncher.launch(arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    ))
                } else {
                    storagePermissionLauncher.launch(arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ))
                }
            }
            "Contacts" -> contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            "Microphone" -> microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            "SMS" -> smsPermissionLauncher.launch(arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS
            ))
            "Enable Notification" -> {
                Log.d("PermissionManager", "launching Enable Notification flow")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // Open notification settings for older versions
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    activity.startActivity(intent)
                }
            }
            "Notification Access" -> {
                // Open notification listener settings so user can grant notification access
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                activity.startActivity(intent)
            }
            else -> {
                // Unknown/unhandled permission name - signal false
                permissionCallback?.invoke(permissionName, false)
            }
        }
    }
}

