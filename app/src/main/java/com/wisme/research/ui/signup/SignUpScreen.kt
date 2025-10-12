package com.wisme.research.ui.signup

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
import com.wisme.research.R
import com.wisme.research.data.local.NavigationState
import com.wisme.research.viewmodel.AuthViewModel
import com.wisme.research.ui.utils.*

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToProfileDetails: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Observe ViewModel state
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val navigationState by authViewModel.navigationState.collectAsStateWithLifecycle()
    val backendHealthy by authViewModel.backendHealthy.collectAsStateWithLifecycle()
    
    // Handle navigation based on auth state
    LaunchedEffect(navigationState) {
        when (navigationState) {
            NavigationState.PROFILE_SETUP -> onNavigateToProfileDetails()
            NavigationState.HOME -> onNavigateToHome()
            else -> { /* Stay on signup */ }
        }
    }
    
    // Error message will persist until user dismisses it manually
    
    SignUpScreenContent(
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToProfileDetails = onNavigateToProfileDetails,
        onNavigateToHome = onNavigateToHome,
        onGoogleSignIn = onGoogleSignIn,
        isLoading = isLoading,
        errorMessage = errorMessage,
        backendHealthy = backendHealthy,
        onSignUpWithEmailPassword = { email, password, _ ->
            authViewModel.signUpWithEmailPassword(email, password)
        },
        onClearError = { authViewModel.clearError() },
        onRetryBackendConnection = { authViewModel.retryBackendConnection() }
    )
}

@Composable
fun SignUpScreenContent(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToProfileDetails: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    backendHealthy: Boolean = true,
    onSignUpWithEmailPassword: (String, String, String) -> Unit = { _, _, _ -> },
    onClearError: () -> Unit = {},
    onRetryBackendConnection: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

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

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun validateForm(): Boolean {
        val emailValidation = validateEmail(email)
        val passwordValidation = validatePassword(password)
        val confirmPasswordValidation = validateConfirmPassword(password, confirmPassword)
        
        // Only show errors if user has attempted to submit
        if (hasAttemptedSubmit) {
            emailError = emailValidation
            passwordError = passwordValidation
            confirmPasswordError = confirmPasswordValidation
        }
        
        return emailValidation == null && passwordValidation == null && confirmPasswordValidation == null
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
            // Backend connectivity status banner - disabled for production to allow Firebase auth
            if (false && !backendHealthy) {
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
                                text = "Connection Issues",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            errorMessage?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
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
            //Sign up with google button
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
                        "Sign up with Google", 
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .matchParentSize(),
                        placeholder = { Text("E-mail", color = textGray, style = MaterialTheme.typography.titleMedium) },
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = if (emailError != null) Color.Red else lightGreen,
                            unfocusedIndicatorColor = if (emailError != null) Color.Red else Color.Transparent
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
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

            Spacer(modifier = Modifier.height(16.dp))

            //Confirm Password
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
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            confirmPasswordError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Confirm Password",
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
                                onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                                modifier = Modifier.padding(end = 17.dp).size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) ImageVector.vectorResource(R.drawable.visibility) else ImageVector.vectorResource(
                                        R.drawable.visibility_off
                                    ),
                                    contentDescription = "Toggle Password Visibility",
                                    tint = Color.White
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(48),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = lightGreen,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = if (confirmPasswordError != null) Color.Red else lightGreen,
                            unfocusedIndicatorColor = if (confirmPasswordError != null) Color.Red else Color.Transparent,
                            focusedLabelColor = textGray,
                            unfocusedLabelColor = textGray
                        ),
                        singleLine = true,
                        isError = confirmPasswordError != null
                    )
                }
                confirmPasswordError?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
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
                        Row {
                            if (error.contains("already registered", ignoreCase = true)) {
                                Button(
                                    onClick = { 
                                        onClearError()
                                        onNavigateToLogin()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = lightGreen
                                    ),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "Sign In",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
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
            }

            Spacer(modifier = Modifier.height(36.dp))

            //Sign Up Button
            Button(
                onClick = { 
                    println("SignUp button clicked - email: $email, password: $password")
                    if (validateFormForSubmit()) {
                        println("Form validation passed, calling onSignUpWithEmailPassword")
                        onSignUpWithEmailPassword(email, password, confirmPassword)
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
                    Text(
                        "Creating account...", 
                        color = Color.Black, 
                        style = ResponsiveTextStyles.labelLarge().copy(fontWeight = FontWeight.Bold)
                    )
                } else {
                    Text(
                        "Sign up for free", 
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
                Text("Already have an account? ", color = Color.White)
                Text(
                    text = "Log in",
                    color = lightGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreenContent(
        onNavigateToLogin = {},
        onNavigateToProfileDetails = {},
        onNavigateToHome = {},
        onGoogleSignIn = {},
        isLoading = false,
        errorMessage = null,
        backendHealthy = true,
        onSignUpWithEmailPassword = { _, _, _ -> },
        onClearError = {},
        onRetryBackendConnection = {}
    )
}
