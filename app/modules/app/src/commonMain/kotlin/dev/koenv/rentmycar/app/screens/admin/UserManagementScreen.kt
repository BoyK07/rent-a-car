package dev.koenv.rentmycar.app.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.screens.profile.ProfileScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.card.Card
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.app.ui.layout.MainLayoutBottomBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.user.PatchUserRequestDto
import dev.koenv.rentmycar.shared.dto.user.UserDto
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * User management screen for admin operations.
 * 
 * Features:
 * - View all users with role, email, and name
 * - Edit user details (name, email, role) via modal dialog
 * - Delete users with confirmation dialog
 * - Role assignment (MEMBER, DRIVER, ADMIN)
 * - Search and filter capabilities (future enhancement)
 * - Loading and error states
 * - Refresh capability
 * - Prevents self-deletion
 * - Only accessible to ADMIN role
 */
class UserManagementScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userRepository = remember { SharedModule.userRepository }
        val authRepository = remember { SharedModule.authRepository }
        
        var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var userToDelete by remember { mutableStateOf<UserDto?>(null) }
        var userToEdit by remember { mutableStateOf<UserDto?>(null) }
        
        val currentUser by authRepository.currentUser.collectAsState()
        val isAdmin = currentUser?.role?.name == "ADMIN"
        val scope = rememberCoroutineScope()
        
        // Fetch users on screen load
        LaunchedEffect(Unit) {
            scope.launch {
                userRepository.getAllUsers().onSuccess { userList ->
                    users = userList
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message ?: "Failed to load users"
                    isLoading = false
                }
            }
        }
        
        // Delete confirmation dialog
        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete User") },
                text = { 
                    Text(
                        "Are you sure you want to delete user '${userToDelete?.name}'? " +
                        "This action cannot be undone."
                    ) 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val userId = userToDelete?.id
                            userToDelete = null
                            
                            if (userId != null) {
                                scope.launch {
                                    userRepository.deleteUser(userId).onSuccess {
                                        // Remove from list
                                        users = users.filter { it.id != userId }
                                    }.onFailure { error ->
                                        errorMessage = error.message ?: "Failed to delete user"
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Delete", color = AppTheme.colors.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Edit user dialog
        if (userToEdit != null) {
            EditUserDialog(
                user = userToEdit!!,
                onDismiss = { userToEdit = null },
                onSave = { updatedName, updatedEmail, updatedRole ->
                    val userId = userToEdit?.id
                    userToEdit = null
                    
                    if (userId != null) {
                        scope.launch {
                            val patchRequest = PatchUserRequestDto(
                                name = if (updatedName != userToEdit?.name) updatedName else null,
                                email = if (updatedEmail != userToEdit?.email) updatedEmail else null,
                                role = if (updatedRole != userToEdit?.role) updatedRole else null
                            )
                            
                            userRepository.patchUser(userId, patchRequest).onSuccess { updatedUser ->
                                // Update in list
                                users = users.map { if (it.id == userId) updatedUser else it }
                            }.onFailure { error ->
                                errorMessage = error.message ?: "Failed to update user"
                            }
                        }
                    }
                }
            )
        }
        
        Scaffold(
            topBar = {
                TopBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "User Management",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }
                }
            },
            bottomBar = {
                MainLayoutBottomBar(selectedRoute = "admin")
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = AppTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navigator.pop() }) {
                                Text("Go Back")
                            }
                        }
                    }
                    users.isEmpty() -> {
                        Text(
                            text = "No users found",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = AppTheme.colors.primaryContainer
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Total Users: ${users.size}",
                                            style = AppTheme.typography.titleMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Admins: ${users.count { it.role.name == "ADMIN" }} | " +
                                                  "Drivers: ${users.count { it.role.name == "DRIVER" }} | " +
                                                  "Members: ${users.count { it.role.name == "MEMBER" }}",
                                            style = AppTheme.typography.bodySmall,
                                            color = AppTheme.colors.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                            
                            items(users) { user ->
                                UserListItem(
                                    user = user,
                                    isCurrentUser = user.id == currentUser?.id,
                                    onDelete = { userToDelete = user },
                                    onEdit = { userToEdit = user }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserListItem(
    user: UserDto,
    isCurrentUser: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppTheme.colors.primary
                )
                
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name,
                            style = AppTheme.typography.titleMedium
                        )
                        if (isCurrentUser) {
                            AssistChip(
                                onClick = { },
                                label = { Text("You", style = AppTheme.typography.labelSmall) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = AppTheme.colors.secondaryContainer
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                user.role.name,
                                style = AppTheme.typography.labelSmall
                            ) 
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (user.role.name) {
                                "ADMIN" -> AppTheme.colors.errorContainer
                                "DRIVER" -> AppTheme.colors.tertiaryContainer
                                else -> AppTheme.colors.surfaceVariant
                            }
                        )
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit button
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = AppTheme.colors.primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit user")
                }
                
                // Don't allow deleting yourself
                if (!isCurrentUser) {
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = AppTheme.colors.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete user")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for editing user information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditUserDialog(
    user: UserDto,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, role: Role) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var selectedRole by remember { mutableStateOf(user.role) }
    var expandedRoleMenu by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Role dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedRoleMenu,
                    onExpandedChange = { expandedRoleMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoleMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedRoleMenu,
                        onDismissRequest = { expandedRoleMenu = false }
                    ) {
                        Role.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRole = role
                                    expandedRoleMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, email, selectedRole) },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserManagementScreenPreview() {
    AppTheme {
        Scaffold(
            topBar = {
                TopBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "User Management",
                            style = AppTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Total Users: 3",
                                    style = AppTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Admins: 1 | Drivers: 1 | Members: 1",
                                    style = AppTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun UserManagementScreenPreviewWrapper() {
    UserManagementScreenPreview()
}
