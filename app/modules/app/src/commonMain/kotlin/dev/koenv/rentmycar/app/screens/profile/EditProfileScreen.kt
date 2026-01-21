package dev.koenv.rentmycar.app.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.IconButton
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.app.ui.components.topbar.TopBar
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.dto.user.PatchUserRequestDto
import kotlinx.coroutines.launch

/**
 * Screen for editing user profile information (name and email).
 * 
 * Features:
 * - Pre-filled form with current user data
 * - Name and email editing (password change not supported)
 * - Input validation with error display
 * - Loading state during data fetch and save
 * - PATCH request for efficient updates
 * - Auto-navigation back on successful save
 * - Requires user to be authenticated
 */
class EditProfileScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { SharedModule.authRepository }
        val userRepository = remember { SharedModule.userRepository }
        
        var isSaving by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        
        // Form fields
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        
        val scope = rememberCoroutineScope()
        
        LaunchedEffect(Unit) {
            val currentUser = authRepository.currentUser.value
            if (currentUser != null) {
                name = currentUser.name
                email = currentUser.email
                isLoading = false
            } else {
                // Try to restore from session
                authRepository.restoreUserSession().onSuccess { user ->
                    name = user.name
                    email = user.email
                    isLoading = false
                }.onFailure {
                    errorMessage = "Failed to load user profile"
                    isLoading = false
                }
            }
        }
        
        Scaffold(
            topBar = {
                TopBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Edit Profile",
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
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null && name.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Error loading profile",
                                style = AppTheme.typography.bodyLarge,
                                color = AppTheme.colors.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navigator.pop() }) {
                                Text("Go Back")
                            }
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Edit your profile information",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            
                            // Name field
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; errorMessage = null },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isSaving,
                                isError = name.isBlank() && errorMessage != null
                            )
                            
                            // Email field
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; errorMessage = null },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !isSaving,
                                isError = email.isBlank() && errorMessage != null
                            )
                            
                            // Error message
                            if (errorMessage != null && name.isNotEmpty()) {
                                Text(
                                    text = errorMessage!!,
                                    color = AppTheme.colors.error,
                                    style = AppTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Save button
                            Button(
                                onClick = {
                                    // Validate inputs
                                    when {
                                        name.isBlank() -> {
                                            errorMessage = "Name cannot be empty"
                                        }
                                        email.isBlank() -> {
                                            errorMessage = "Email cannot be empty"
                                        }
                                        !email.contains("@") || !email.contains(".") -> {
                                            errorMessage = "Please enter a valid email"
                                        }
                                        else -> {
                                            errorMessage = null
                                            isSaving = true
                                            
                                            scope.launch {
                                                val currentUser = authRepository.currentUser.value
                                                if (currentUser == null) {
                                                    errorMessage = "User session expired. Please log in again."
                                                    isSaving = false
                                                    return@launch
                                                }
                                                
                                                val patchRequest = PatchUserRequestDto(
                                                    name = if (name != currentUser.name) name else null,
                                                    email = if (email != currentUser.email) email else null,
                                                    role = null // Don't allow role changes
                                                )
                                                
                                                userRepository.patchUser(currentUser.id, patchRequest).onSuccess { updatedUser ->
                                                    // Update auth repository with new user data
                                                    authRepository.updateCurrentUser(updatedUser)
                                                    navigator.pop()
                                                }.onFailure { error ->
                                                    errorMessage = error.message ?: "Failed to update profile"
                                                    isSaving = false
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = AppTheme.colors.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isSaving) "Saving..." else "Save Changes")
                            }
                            
                            // Cancel button
                            OutlinedButton(
                                onClick = { navigator.pop() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSaving
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}
