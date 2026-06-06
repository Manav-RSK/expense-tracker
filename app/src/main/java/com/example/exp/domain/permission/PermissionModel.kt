package com.example.exp.domain.permission

import android.Manifest
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a permission
 */
data class Permission(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val isGranted: Boolean = false,
    val isSpecialPermission: Boolean = false,
    val isDisabled: Boolean = false,
    val isSelected: Boolean = false,
    val androidPermissions: List<String> = emptyList()
)

/**
 * Get the minimal set of permissions required by the app.
 *
 * We only expose: Microphone, Notifications, Storage, Contacts, SMS
 */
fun getAllPermissions(): List<Permission> {
    return listOf(
        Permission(
            "Microphone",
            Icons.Default.Settings,
            "Record audio",
            androidPermissions = listOf(Manifest.permission.RECORD_AUDIO)
        ),
        Permission(
            "Enable Notification",
            Icons.Default.Notifications,
            "Show and post notifications",
            androidPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            } else emptyList()
        ),
        Permission(
            "Notification Access",
            Icons.Default.Notifications,
            "Allow app to read notifications",
            isSpecialPermission = true
        ),
        Permission(
            "Storage",
            Icons.Default.Info,
            "Access device storage",
            androidPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        ),
        Permission(
            "Contacts",
            Icons.Default.Person,
            "Access your contacts",
            androidPermissions = listOf(Manifest.permission.READ_CONTACTS)
        ),
        Permission(
            "SMS",
            Icons.Default.Email,
            "Send and view SMS messages",
            androidPermissions = listOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS)
        )
    )
}

