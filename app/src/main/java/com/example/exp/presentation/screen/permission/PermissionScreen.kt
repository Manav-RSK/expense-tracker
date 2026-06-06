package com.example.exp.presentation.screen.permission

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.exp.domain.permission.Permission
import com.example.exp.domain.permission.PermissionManager
import com.example.exp.domain.permission.getAllPermissions
import com.example.exp.data.repository.UsageRepository
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleEventObserver


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    activity: ComponentActivity,
    permissionManager: PermissionManager,
    onContinue: () -> Unit = {}
) {
    val context = LocalContext.current
    val usageRepository = remember { UsageRepository(context) }

    // Track when we return from settings to check permission status
    var checkPermissions by remember { mutableStateOf(false) }

    // Track if this is the initial load
    var isInitialLoad by remember { mutableStateOf(true) }

    // Lifecycle observer to check permissions when returning from settings
    DisposableEffect(activity) {
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                checkPermissions = true
            }
        }
        activity.lifecycle.addObserver(lifecycleObserver)
        onDispose { activity.lifecycle.removeObserver(lifecycleObserver) }
    }

    val permissions = remember {
        mutableStateListOf(*getAllPermissions().toTypedArray())
    }

    // Check all permissions status when returning from settings
    LaunchedEffect(checkPermissions) {
        if (checkPermissions && !isInitialLoad) {
            permissions.forEachIndexed { index, permission ->
                val isGranted = permissionManager.permissionChecker.checkPermissionStatus(permission)
                permissions[index] = permissions[index].copy(
                    isGranted = isGranted,
                    isDisabled = isGranted
                )
            }
            checkPermissions = false
        }
    }

    // Initial permission check on first load
    LaunchedEffect(Unit) {
        permissions.forEachIndexed { index, permission ->
            val isGranted = permissionManager.permissionChecker.checkPermissionStatus(permission)
            permissions[index] = permissions[index].copy(
                isGranted = isGranted,
                isDisabled = isGranted
            )
        }

        // Debug: log initial permission statuses
        permissions.forEach { p ->
            android.util.Log.d("PermissionsScreen", "Initial status - ${p.name}: granted=${p.isGranted}")
        }

        // Check if all permissions are already granted on initial load
        val allGranted = permissions.all { it.isGranted }
        if (allGranted) {
            // All permissions already granted, navigate directly to home screen
            onContinue()
        } else {
            // Not all granted, mark initial load complete
            isInitialLoad = false
        }
    }

    val allPermissionsGranted = permissions.all { it.isGranted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Permissions Required",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Text
            Text(
                text = "Please grant the following permissions to use this app",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Scrollable Permission List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(permissions) { index, permission ->
                    PermissionItem(
                        permission = permission,
                        onToggle = {
                            // Toggle selection state (UI only). Do not request system permission yet.
                            val p = permissions[index]
                            permissions[index] = p.copy(isSelected = !p.isSelected)
                        }
                    )
                }

                // Add spacing at the bottom of the list
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Continue / Grant All Permissions Button
            val allSelected = permissions.all { it.isGranted || it.isSelected }
            Button(
                onClick = {
                    if (allPermissionsGranted) {
                        // All already granted -> continue
                        usageRepository.logCompleteUsageData()
                        onContinue()
                        return@Button
                    }

                    // Request system permissions for the permissions the user selected (and which are not already granted).
                    val toRequest = permissions.withIndex()
                        .filter { (_, p) -> p.isSelected && !p.isGranted }
                        .map { it.index }

                    fun requestNext(indexInList: Int) {
                        if (indexInList >= toRequest.size) {
                            // After requesting all, refresh statuses. Do NOT auto-navigate.
                            // The UI will show granted states and the button will change to "Continue".
                            return
                        }

                        val permIndex = toRequest[indexInList]
                        val perm = permissions[permIndex]
                        // Use PermissionManager to request this permission; callback will update state and continue sequentially
                        android.util.Log.d("PermissionsScreen", "Requesting permission: ${perm.name}")
                        permissionManager.requestPermission(perm.name) { permissionName, isGranted ->
                            val idx = permissions.indexOfFirst { it.name == permissionName }
                            if (idx != -1) {
                                permissions[idx] = permissions[idx].copy(
                                    isGranted = isGranted,
                                    isDisabled = isGranted,
                                    isSelected = false
                                )
                            }
                            // proceed to next
                            requestNext(indexInList + 1)
                        }
                    }

                    if (toRequest.isEmpty()) {
                        // Nothing to request — maybe already granted or nothing selected. If all granted navigate.
                        if (permissions.all { it.isGranted }) {
                            usageRepository.logCompleteUsageData()
                            onContinue()
                        }
                    } else {
                        requestNext(0)
                    }
                },
                enabled = allSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = if (allPermissionsGranted) "Continue" else "Grant All Permissions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PermissionItem(
    permission: Permission,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = permission.icon,
                contentDescription = permission.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = permission.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = permission.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Checkbox(
                checked = permission.isGranted || permission.isSelected,
                onCheckedChange = { if (!permission.isDisabled) onToggle() },
                enabled = !permission.isDisabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledCheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionMainScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kiosk Assist",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to Kiosk Assist!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "All permissions granted successfully",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }

}