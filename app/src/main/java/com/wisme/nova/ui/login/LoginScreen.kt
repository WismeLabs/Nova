package com.wisme.nova.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.R

@Composable
fun LoginScreen() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val lightGreen = Color(0xFFC1FF72)
    val darkGreen = Color(0x33C0F062)
    val textGray = Color(0xFF9E9E9E)

    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 19.dp),
        ) {
            Spacer(modifier = Modifier.height(235.dp))
            //Sign in with google button
            Button(
                onClick = {/*TODO: google sign in*/},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp),
                shape = RoundedCornerShape(48),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
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
                    Text("Sign in with Google", color = Color.Black, fontSize = 16.sp, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            //"OR" Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Divider(color = Color.Gray, modifier = Modifier.weight(1f))
                Text(
                    "OR",
                    color = textGray,
                    modifier = Modifier.padding(horizontal = 5.dp),
                    fontSize = 14.sp
                )
                Divider(color = Color.Gray, modifier = Modifier.weight(1f))
            }
            //Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("E-mail", color = textGray, style = MaterialTheme.typography.titleMedium) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = textGray
                    )
                },
                shape = RoundedCornerShape(48),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = lightGreen,
                    focusedContainerColor = darkGreen,
                    unfocusedContainerColor = darkGreen,
                    focusedIndicatorColor = lightGreen,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = textGray,
                    unfocusedLabelColor = textGray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            //Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Password", color = textGray, style = MaterialTheme.typography.titleMedium) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        tint = textGray
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible },modifier=Modifier.padding(end=17.dp).size(20.dp)) {
                        Icon(
                            imageVector = if (isPasswordVisible) ImageVector.vectorResource(  R.drawable.visibility) else ImageVector.vectorResource(R.drawable.visibility_off),
                            contentDescription = "Toggle Password Visibility",
                            tint = textGray
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
                    focusedIndicatorColor = lightGreen,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLabelColor = textGray,
                    unfocusedLabelColor = textGray
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(36.dp))

            //Sign In Button
            Button(
                onClick = { /* TODO:Sign in*/},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp),
                shape = RoundedCornerShape(48),
                colors = ButtonDefaults.buttonColors(containerColor = lightGreen)
            ) {
                Text("Sign in", color = Color.Black, fontSize = 18.sp, style=MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
                ClickableText(
                    text = AnnotatedString("Sign up"),
                    onClick = { /* TODO: Navigate to Sign Up Screen */ },
                    style = TextStyle(
                        color = lightGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
