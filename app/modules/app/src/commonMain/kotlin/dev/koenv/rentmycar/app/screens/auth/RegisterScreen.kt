package dev.koenv.rentmycar.app.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.koenv.rentmycar.app.screens.home.HomeScreen
import dev.koenv.rentmycar.app.ui.AppTheme
import dev.koenv.rentmycar.app.ui.components.Button
import dev.koenv.rentmycar.app.ui.components.ButtonVariant
import dev.koenv.rentmycar.app.ui.components.Scaffold
import dev.koenv.rentmycar.app.ui.components.Switch
import dev.koenv.rentmycar.app.ui.components.Text
import dev.koenv.rentmycar.shared.SharedModule
import dev.koenv.rentmycar.shared.domain.enums.Role
import dev.koenv.rentmycar.shared.dto.auth.RegisterRequestDto
import dev.koenv.rentmycar.shared.repository.AuthState
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Registration screen for new users.
 * Allows optional role selection (MEMBER or DRIVER).
 */
class RegisterScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = remember { SharedModule.authRepository }
        val authState by authRepository.authState.collectAsState()
        
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf<Role?>(Role.MEMBER) } // Default to MEMBER
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        
        val scope = rememberCoroutineScope()
        
        // Navigate to home when authenticated
        LaunchedEffect(authState) {
            if (authState == AuthState.Authenticated) {
                navigator.replaceAll(HomeScreen())
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = AppTheme.typography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Join Rent My Car",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.outline,
                    focusedLabelColor = AppTheme.colors.primary,
                    unfocusedLabelColor = AppTheme.colors.onSurfaceVariant,
                    cursorColor = AppTheme.colors.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.outline,
                    focusedLabelColor = AppTheme.colors.primary,
                    unfocusedLabelColor = AppTheme.colors.onSurfaceVariant,
                    cursorColor = AppTheme.colors.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.outline,
                    focusedLabelColor = AppTheme.colors.primary,
                    unfocusedLabelColor = AppTheme.colors.onSurfaceVariant,
                    cursorColor = AppTheme.colors.primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.outline,
                    focusedLabelColor = AppTheme.colors.primary,
                    unfocusedLabelColor = AppTheme.colors.onSurfaceVariant,
                    cursorColor = AppTheme.colors.primary
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Role selection (optional)
            Text(
                text = "Select Role (Optional)",
                style = AppTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedRole == Role.MEMBER,
                    onClick = { selectedRole = if (selectedRole == Role.MEMBER) null else Role.MEMBER },
                    label = { Text("Member") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
                
                FilterChip(
                    selected = selectedRole == Role.DRIVER,
                    onClick = { selectedRole = if (selectedRole == Role.DRIVER) null else Role.DRIVER },
                    label = { Text("Driver") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                )
            }
            
            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage ?: "",
                    color = AppTheme.colors.error,
                    style = AppTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Register button
            Button(
                onClick = {
                    // Validation
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() -> {
                            errorMessage = "Please fill in all required fields"
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }
                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                            return@Button
                        }
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        val result = authRepository.register(
                            RegisterRequestDto(
                                name = name,
                                email = email,
                                password = password,
                                role = selectedRole
                            )
                        )
                        
                        result.onFailure { error ->
                            errorMessage = error.message ?: "Registration failed"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppTheme.colors.onPrimary
                    )
                } else {
                    Text("Register")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Navigate to login
            Button(
                onClick = { navigator.pop() },
                enabled = !isLoading,
                variant = ButtonVariant.Ghost
            ) {
                Text("Already have an account? Login")
            }
        }
    }
}

@Composable
private fun RegisterScreenPreview() {
    AppTheme {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf<Role?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create Account",
                style = AppTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join Rent My Car",
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select Role (Optional)",
                style = AppTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedRole == Role.MEMBER,
                    onClick = { selectedRole = if (selectedRole == Role.MEMBER) null else Role.MEMBER },
                    label = { Text("Member") },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedRole == Role.DRIVER,
                    onClick = { selectedRole = if (selectedRole == Role.DRIVER) null else Role.DRIVER },
                    label = { Text("Driver") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                variant = ButtonVariant.Ghost
            ) {
                Text("Already have an account? Login")
            }
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreviewWrapper() {
    RegisterScreenPreview()
}
