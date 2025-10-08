package com.wisme.firstapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisme.firstapp.R
import com.wisme.firstapp.data.local.NavigationState
import com.wisme.firstapp.viewmodel.AuthViewModel
import com.wisme.firstapp.ui.utils.*

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfileSetup: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    // Observe ViewModel state
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val navigationState by authViewModel.navigationState.collectAsStateWithLifecycle()
    val backendHealthy by authViewModel.backendHealthy.collectAsStateWithLifecycle()
    val passwordResetSuccess by authViewModel.passwordResetSuccess.collectAsStateWithLifecycle()
    val passwordResetError by authViewModel.passwordResetError.collectAsStateWithLifecycle()
    
    // Handle navigation based on auth state
    LaunchedEffect(navigationState) {
        when (navigationState) {
            NavigationState.PROFILE_SETUP -> onNavigateToProfileSetup()
            NavigationState.HOME -> onNavigateToHome()
            else -> { /* Stay on login */ }
        }
    }
    
    // Error message will persist until user dismisses it manually
    
    LoginScreenContent(
        onNavigateToSignUp = onNavigateToSignUp,
        onNavigateToHome = onNavigateToHome,
        onNavigateToProfileSetup = onNavigateToProfileSetup,
        onGoogleSignIn = onGoogleSignIn,
        isLoading = isLoading,
        errorMessage = errorMessage,
        backendHealthy = backendHealthy,
        passwordResetSuccess = passwordResetSuccess,
        passwordResetError = passwordResetError,
        onSignInWithEmailPassword = { email, password ->
            authViewModel.signInWithEmailPassword(email, password)
        },
        onSendPasswordResetEmail = { email ->
            authViewModel.sendPasswordResetEmail(email)
        },
        onClearError = { authViewModel.clearError() },
        onClearPasswordResetSuccess = { authViewModel.clearPasswordResetSuccess() },
        onClearPasswordResetError = { authViewModel.clearPasswordResetError() },
        onRetryBackendConnection = { authViewModel.retryBackendConnection() }
    )
}

@Composable
fun LoginScreenContent(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToProfileSetup: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    backendHealthy: Boolean = true,
    passwordResetSuccess: Boolean = false,
    passwordResetError: String? = null,
    onSignInWithEmailPassword: (String, String) -> Unit = { _, _ -> },
    onSendPasswordResetEmail: (String) -> Unit = {},
    onClearError: () -> Unit = {},
    onClearPasswordResetSuccess: () -> Unit = {},
    onClearPasswordResetError: () -> Unit = {},
    onRetryBackendConnection: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showPasswordResetSuccessDialog by remember { mutableStateOf(false) }
    
    // Handle password reset success
    LaunchedEffect(passwordResetSuccess) {
        if (passwordResetSuccess) {
            showPasswordResetDialog = false // Close the reset dialog
            showPasswordResetSuccessDialog = true
            onClearPasswordResetSuccess()
        }
    }

    val lightGreen = Color(0xFFC1FF72)
    val darkGreen = Color(0x33C0F062)
    val textGray = Color(0xFF9E9E9E)

    // Validation functions
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateForm(): Boolean {
        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)
        
        // Only show errors if user has attempted to submit
        if (hasAttemptedSubmit) {
            emailError = emailValidation
            passwordError = passwordValidation
        }
        
        return emailValidation == null && passwordValidation == null
    }
    
    fun validateFormForSubmit(): Boolean {
        println("validateFormForSubmit called")
        hasAttemptedSubmit = true
        val result = validateForm()
        println("validateFormForSubmit result: $result")
        return result
    }

    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 19.dp),
        ) {
            // Backend connectivity status banner - non-blocking info only
            if (!backendHealthy) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            style = ResponsiveTextStyles.titleMedium()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Server Sync Offline",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "You can still sign in. Data will sync when connection is restored.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = { onRetryBackendConnection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Retry",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(235.dp))
            //Sign in with google button
            Button(
                onClick = { 
                    onGoogleSignIn()
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp),
                shape = RoundedCornerShape(48),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.google_logo),
                        contentDescription = "Google Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Sign in with Google", 
                        color = Color.Black, 
                        style = ResponsiveTextStyles.bodyLarge().copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            //"OR" Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                HorizontalDivider(color = Color.Gray, modifier = Modifier.weight(1f))
                Text(
                    "OR",
                    color = textGray,
                    modifier = Modifier.padding(horizontal = 5.dp),
                    style = ResponsiveTextStyles.labelLarge()
                )
                HorizontalDivider(color = Color.Gray, modifier = Modifier.weight(1f))
            }
            //Email
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.text_box_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(48))
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            emailError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "E-mail",
                                color = textGray,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.email_icon),
                                contentDescription = "Email Icon",
                                tint = Color.Unspecified
                            )
                        },
                        shape = RoundedCornerShape(48),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = lightGreen,
                            focusedContainerColor = darkGreen,
                            unfocusedContainerColor = darkGreen,
                            focusedIndicatorColor = if (emailError != null) Color.Red else lightGreen,
                            unfocusedIndicatorColor = if (emailError != null) Color.Red else Color.Transparent,
                            focusedLabelColor = textGray,
                            unfocusedLabelColor = textGray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = emailError != null
                    )
                }
                emailError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Password
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.text_box_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(48))
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            passwordError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Password",
                                color = textGray,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painterResource(R.drawable.lock_icon),
                                contentDescription = "Password Icon",
                                tint = Color.Unspecified
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.padding(end = 17.dp).size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) ImageVector.vectorResource(R.drawable.visibility) else ImageVector.vectorResource(
                                        R.drawable.visibility_off
                                    ),
                                    contentDescription = "Toggle Password Visibility",
                                    tint = Color.White
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(48),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = lightGreen,
                            focusedContainerColor = darkGreen,
                            unfocusedContainerColor = darkGreen,
                            focusedIndicatorColor = if (passwordError != null) Color.Red else lightGreen,
                            unfocusedIndicatorColor = if (passwordError != null) Color.Red else Color.Transparent,
                            focusedLabelColor = textGray,
                            unfocusedLabelColor = textGray
                        ),
                        singleLine = true,
                        isError = passwordError != null
                    )
                }
                passwordError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Forgot Password Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    color = lightGreen,
                    style = ResponsiveTextStyles.labelLarge().copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.clickable { 
                        // Show password reset dialog
                        showPasswordResetDialog = true
                    }
                )
            }

            // Authentication Error Display
            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "❌",
                            style = ResponsiveTextStyles.titleMedium()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Authentication Error",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Button(
                            onClick = { onClearError() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Dismiss",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //Sign In Button
            Button(
                onClick = { 
                    println("Login button clicked - email: $email, password: $password")
                    if (validateFormForSubmit()) {
                        println("Form validation passed, calling onSignInWithEmailPassword")
                        onSignInWithEmailPassword(email, password)
                    } else {
                        println("Form validation failed")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp),
                shape = RoundedCornerShape(48),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lightGreen,
                    disabledContainerColor = Color.Gray
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Signing in...", 
                            color = Color.Black, 
                            style = ResponsiveTextStyles.labelLarge().copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Text(
                        "Sign in", 
                        color = Color.Black, 
                        style = ResponsiveTextStyles.labelLarge().copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(35.dp))

            //Sign Up Text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account? ", color = Color.White)
                Text(
                    text = "Sign up",
                    color = lightGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
        
        // Password Reset Dialog
        if (showPasswordResetDialog) {
            PasswordResetDialog(
                onDismiss = { 
                    showPasswordResetDialog = false
                    onClearPasswordResetError()
                },
                onResetPassword = { resetEmail ->
                    onSendPasswordResetEmail(resetEmail)
                },
                isLoading = isLoading,
                errorMessage = passwordResetError
            )
        }
        
        // Password Reset Success Dialog
        if (showPasswordResetSuccessDialog) {
            PasswordResetSuccessDialog(
                onDismiss = { showPasswordResetSuccessDialog = false }
            )
        }
    }
}

@Composable
fun PasswordResetDialog(
    onDismiss: () -> Unit,
    onResetPassword: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var resetEmail by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val lightGreen = Color(0xFFC1FF72)
    val textGray = Color(0xFF9E9E9E)
    
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset Password",
                    color = Color.White,
                    style = ResponsiveTextStyles.titleLarge().copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    color = textGray,
                    style = ResponsiveTextStyles.bodyMedium(),
                    modifier = Modifier.padding(bottom = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                // Email input
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.text_box_bg),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(48))
                        )
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { 
                                resetEmail = it
                                emailError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "E-mail",
                                    color = textGray,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.email_icon),
                                    contentDescription = "Email Icon",
                                    tint = textGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = if (emailError != null) Color.Red else Color.Transparent,
                                unfocusedIndicatorColor = if (emailError != null) Color.Red else Color.Transparent,
                                focusedLabelColor = textGray,
                                unfocusedLabelColor = textGray
                            ),
                            singleLine = true,
                            isError = emailError != null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }
                    emailError?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = ResponsiveFontSizes.caption(),
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
                
                // Show password reset error from ViewModel
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = ResponsiveFontSizes.bodySmall(),
                        modifier = Modifier.padding(top = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(48),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Send Reset Email Button
                    Button(
                        onClick = {
                            emailError = validateEmail(resetEmail)
                            if (emailError == null) {
                                onResetPassword(resetEmail)
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(48),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = lightGreen,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        if (isLoading) {
                            Text(
                                text = "Sending...",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Send Reset Link",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordResetSuccessDialog(
    onDismiss: () -> Unit
) {
    val lightGreen = Color(0xFFC1FF72)
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon (using emoji for now)
                Text(
                    text = "✅",
                    fontSize = ResponsiveFontSizes.displayLarge(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Email Sent!",
                    color = Color.White,
                    fontSize = ResponsiveFontSizes.heading(),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Text(
                    text = "We've sent you a password reset link. Check your email inbox and follow the instructions to reset your password.",
                    color = Color.Gray,
                    fontSize = ResponsiveFontSizes.bodySmall(),
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(48),
                    colors = ButtonDefaults.buttonColors(containerColor = lightGreen)
                ) {
                    Text(
                        text = "Got it!",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreenContent(
        onNavigateToSignUp = {},
        onNavigateToHome = {},
        onNavigateToProfileSetup = {},
        onGoogleSignIn = {},
        isLoading = false,
        errorMessage = null,
        backendHealthy = true,
        passwordResetSuccess = false,
        passwordResetError = null,
        onSignInWithEmailPassword = { _, _ -> },
        onSendPasswordResetEmail = { },
        onClearError = {},
        onClearPasswordResetSuccess = {},
        onClearPasswordResetError = {},
        onRetryBackendConnection = {}
    )
}
