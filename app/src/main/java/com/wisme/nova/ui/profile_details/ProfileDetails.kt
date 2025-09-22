package com.wisme.nova.ui.profile_details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wisme.nova.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen() {
    var name by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedProfession by remember { mutableStateOf("") }
    var professionExpanded by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(R.drawable.avatar_1) }

    val avatars = listOf(
        R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3,
        R.drawable.avatar_4, R.drawable.avatar_5
    )
    val lightGreen = Color(0xFFC1FF72)
    val textGray = Color.Gray
    val darkGreen = Color(0xFF1A241F)

    Surface(
        color = Color.Black,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            //Big avatar display
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = selectedAvatar),
                    contentDescription = "Selected Avatar",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Choose avatar", color = Color.White,style=MaterialTheme.typography.titleMedium, fontSize = 20.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))

            //Avatar row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                avatars.forEach { avatarRes ->
                    AvatarItem(
                        avatarRes = avatarRes,
                        isSelected = selectedAvatar == avatarRes,
                        onSelect = { selectedAvatar = avatarRes }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Other details", color = Color.White,style=MaterialTheme.typography.titleMedium, fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            //Name
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.text_box_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your name", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                    leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                    shape = RoundedCornerShape(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //DisplayName
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.text_box_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Display name", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                    leadingIcon = { Icon(painterResource(id = R.drawable.lock_icon), contentDescription = null, tint = Color.Unspecified) },
                    shape = RoundedCornerShape(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            //Date of birth
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.text_box_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Date of Birth", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                    leadingIcon = { Icon(painterResource(id = R.drawable.date), contentDescription = null, tint = Color.Unspecified) },
                    trailingIcon = { Icon(painterResource(id = R.drawable.calendar), contentDescription = null, tint = Color.Unspecified) },
                    shape = RoundedCornerShape(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                //Gender
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = !genderExpanded },
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(56.dp).menuAnchor()) {
                            Image(
                                painter = painterResource(id = R.drawable.text_box_bg),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                            )
                            TextField(
                                value = selectedGender.ifEmpty { "Gender" },
                                textStyle = MaterialTheme.typography.titleMedium,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                shape = RoundedCornerShape(48.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = if (selectedGender.isEmpty()) textGray else Color.White,
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                    focusedTrailingIconColor = lightGreen, unfocusedTrailingIconColor = lightGreen
                                ),
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false },
                            modifier = Modifier.background(darkGreen)
                        ) {
                            listOf("Male", "Female").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Color.White) },
                                    onClick = { selectedGender = item; genderExpanded = false }
                                )
                            }
                        }
                    }
                }
                //Profession
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = professionExpanded,
                        onExpandedChange = { professionExpanded = !professionExpanded },
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(56.dp).menuAnchor()) {
                            Image(
                                painter = painterResource(id = R.drawable.text_box_bg),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                            )
                            TextField(
                                value = selectedProfession.ifEmpty { "Profession" },
                                textStyle = MaterialTheme.typography.titleMedium,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionExpanded) },
                                shape = RoundedCornerShape(48.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White, unfocusedTextColor = if (selectedProfession.isEmpty()) textGray else Color.White,
                                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                    focusedTrailingIconColor = lightGreen, unfocusedTrailingIconColor = lightGreen
                                ),
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = professionExpanded,
                            onDismissRequest = { professionExpanded = false },
                            modifier = Modifier.background(darkGreen)
                        ) {
                            listOf("Engineer", "Doctor", "Artist").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Color.White) },
                                    onClick = { selectedProfession = item; professionExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = lightGreen)
            ) {
                Text("Next", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

//Avatar row items
@Composable
fun AvatarItem(
    avatarRes: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .clickable { onSelect() }
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFFC1FF72) else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = avatarRes),
            contentDescription = "Avatar",
            modifier = Modifier.fillMaxSize()
        )
        if (isSelected) {
            Box(
                modifier = Modifier.matchParentSize().background(Color(0xFFC1FF72).copy(alpha = 0.5f))
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = "Selected",
                tint = Color.Unspecified
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun ProfileDetailsScreenPreview() {
    ProfileDetailsScreen()
}
