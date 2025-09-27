package com.wisme.firstapp.ui.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisme.firstapp.R
import com.wisme.firstapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authPrefs = authViewModel.getAuthPreferences()
    
    // State variables for editing
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(authPrefs.userName ?: "") }
    var displayName by remember { mutableStateOf(authPrefs.userDisplayName ?: "") }
    var dob by remember { mutableStateOf(authPrefs.userDateOfBirth ?: "") }
    var selectedGender by remember { mutableStateOf(authPrefs.userGender ?: "") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedProfession by remember { mutableStateOf(authPrefs.userProfession ?: "") }
    var professionExpanded by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(authPrefs.userAvatarId ?: 1) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE4FFC2), Color(0xFFC1FF72))
    )
    
    // Avatar mapping
    val avatarResource = when (selectedAvatar) {
        1 -> R.drawable.avatar_1
        2 -> R.drawable.avatar_2
        3 -> R.drawable.avatar_3
        4 -> R.drawable.avatar_4
        5 -> R.drawable.avatar_5
        else -> R.drawable.avatar_1
    }
    
    // Gender options (matching ProfileDetails exactly)
    val genderOptions = listOf("male", "female", "other", "prefer_not_to_say")
    
    // Profession options (matching ProfileDetails exactly)
    val professionOptions = listOf("School Student", "UG Student", "PG Student", "PhD Scholar", "Working Professional", "Other")
    
    // Date picker setup
    val currentCalendar = java.util.Calendar.getInstance()
    val maxDateCalendar = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.YEAR, -13)
    }
    val minDateCalendar = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.YEAR, -120)
    }
    
    val datePickerState = rememberDatePickerState(
        yearRange = minDateCalendar.get(java.util.Calendar.YEAR)..maxDateCalendar.get(java.util.Calendar.YEAR)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        // Black background header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.Black)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header with back button and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Profile",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                
                // Edit/Save button
                TextButton(
                    onClick = {
                        if (isEditing) {
                            // Validate and save
                            var hasErrors = false
                            
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                hasErrors = true
                            } else {
                                nameError = null
                            }
                            
                            if (displayName.isBlank()) {
                                displayNameError = "Display name is required"
                                hasErrors = true
                            } else {
                                displayNameError = null
                            }
                            
                            if (dob.isBlank()) {
                                dobError = "Date of birth is required"
                                hasErrors = true
                            } else {
                                dobError = null
                            }
                            
                            if (!hasErrors) {
                                // Save to local preferences
                                authPrefs.apply {
                                    userName = name
                                    userDisplayName = displayName
                                    userDateOfBirth = dob
                                    userGender = selectedGender
                                    userProfession = selectedProfession
                                    userAvatarId = selectedAvatar
                                }
                                
                                // Update online via AuthViewModel
                                authViewModel.updateUserProfile(
                                    name = name,
                                    displayName = displayName,
                                    dateOfBirth = dob,
                                    gender = selectedGender,
                                    profession = selectedProfession,
                                    avatarId = selectedAvatar
                                )
                                
                                isEditing = false
                                focusManager.clearFocus()
                            }
                        } else {
                            isEditing = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit",
                        tint = Color(0xFFC1FF72),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditing) "Save" else "Edit",
                        color = Color(0xFFC1FF72),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar section with selection in edit mode
            if (isEditing) {
                AvatarSelectionSection(
                    selectedAvatar = selectedAvatar,
                    onAvatarSelected = { selectedAvatar = it }
                )
            } else {
                Image(
                    painter = painterResource(id = avatarResource),
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isEditing) {
                Text(
                    text = displayName.ifBlank { name },
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Profile details section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 32.dp, vertical = 37.dp)
            ) {
                Text(
                    text = "Profile Details",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Divider(Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isEditing) {
                    // Edit mode with text fields
                    EditableProfileFields(
                        name = name,
                        onNameChange = { name = it; nameError = null },
                        nameError = nameError,
                        displayName = displayName,
                        onDisplayNameChange = { displayName = it; displayNameError = null },
                        displayNameError = displayNameError,
                        dob = dob,
                        onDobClick = { showDatePicker = true },
                        dobError = dobError,
                        selectedGender = selectedGender,
                        onGenderChange = { selectedGender = it },
                        genderExpanded = genderExpanded,
                        onGenderExpandedChange = { genderExpanded = it },
                        genderOptions = genderOptions,
                        selectedProfession = selectedProfession,
                        onProfessionChange = { selectedProfession = it },
                        professionExpanded = professionExpanded,
                        onProfessionExpandedChange = { professionExpanded = it },
                        professionOptions = professionOptions
                    )
                } else {
                    // Display mode
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ProfileDetailRow(label = "Name", value = name.ifBlank { "Not set" })
                        ProfileDetailRow(label = "Display Name", value = displayName.ifBlank { "Not set" })
                        ProfileDetailRow(label = "Date of Birth", value = dob.ifBlank { "Not set" })
                        ProfileDetailRow(label = "Gender", value = selectedGender.ifBlank { "Not set" })
                        ProfileDetailRow(label = "Profession", value = selectedProfession.ifBlank { "Not set" })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign out button
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(49.dp)
                        .background(gradient, shape = RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = java.util.Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            val month = calendar.get(java.util.Calendar.MONTH) + 1
                            val year = calendar.get(java.util.Calendar.YEAR)
                            dob = String.format("%02d/%02d/%d", day, month, year)
                            dobError = null
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = Color(0xFFC1FF72))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1C1C1E)
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1C1C1E),
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color.White,
                    subheadContentColor = Color.White,
                    yearContentColor = Color.White,
                    currentYearContentColor = Color(0xFFC1FF72),
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = Color(0xFFC1FF72),
                    dayContentColor = Color.White,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = Color(0xFFC1FF72),
                    todayContentColor = Color(0xFFC1FF72),
                    todayDateBorderColor = Color(0xFFC1FF72)
                )
            )
        }
    }
}

//Each detail
@Composable
fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            style=MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = Color.White,
            style=MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}


@Composable
fun AvatarSelectionSection(
    selectedAvatar: Int,
    onAvatarSelected: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Current selected avatar (large)
        val avatarResource = when (selectedAvatar) {
            1 -> R.drawable.avatar_1
            2 -> R.drawable.avatar_2
            3 -> R.drawable.avatar_3
            4 -> R.drawable.avatar_4
            5 -> R.drawable.avatar_5
            else -> R.drawable.avatar_1
        }
        
        Image(
            painter = painterResource(id = avatarResource),
            contentDescription = "Selected Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Choose Avatar",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Avatar options row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            for (avatarId in 1..5) {
                val avatarRes = when (avatarId) {
                    1 -> R.drawable.avatar_1
                    2 -> R.drawable.avatar_2
                    3 -> R.drawable.avatar_3
                    4 -> R.drawable.avatar_4
                    5 -> R.drawable.avatar_5
                    else -> R.drawable.avatar_1
                }
                
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selectedAvatar == avatarId) 3.dp else 1.dp,
                            color = if (selectedAvatar == avatarId) Color(0xFFC1FF72) else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(avatarId) }
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Avatar $avatarId",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableProfileFields(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    displayNameError: String?,
    dob: String,
    onDobClick: () -> Unit,
    dobError: String?,
    selectedGender: String,
    onGenderChange: (String) -> Unit,
    genderExpanded: Boolean,
    onGenderExpandedChange: (Boolean) -> Unit,
    genderOptions: List<String>,
    selectedProfession: String,
    onProfessionChange: (String) -> Unit,
    professionExpanded: Boolean,
    onProfessionExpandedChange: (Boolean) -> Unit,
    professionOptions: List<String>
) {
    val lightGreen = Color(0xFFC1FF72)
    val textGray = Color.Gray
    val darkGreen = Color(0xFF1A241F)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name Field (matching ProfileDetails exactly)
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.text_box_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Your name", color = textGray, style = MaterialTheme.typography.titleMedium, fontSize = 16.sp) },
                    leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                    shape = RoundedCornerShape(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = if (nameError != null) Color.Red else Color.Transparent,
                        unfocusedIndicatorColor = if (nameError != null) Color.Red else Color.Transparent,
                    ),
                    singleLine = true,
                    isError = nameError != null
                )
            }
            nameError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // Display Name Field (matching ProfileDetails exactly)
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.text_box_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Display name", color = textGray, style = MaterialTheme.typography.titleMedium, fontSize = 16.sp) },
                    leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                    shape = RoundedCornerShape(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = if (displayNameError != null) Color.Red else Color.Transparent,
                        unfocusedIndicatorColor = if (displayNameError != null) Color.Red else Color.Transparent,
                    ),
                    singleLine = true,
                    isError = displayNameError != null
                )
            }
            displayNameError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // Date of Birth Field (matching ProfileDetails exactly)
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onDobClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.text_box_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                )
                
                // Custom date field that looks like a text field but is clickable
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painterResource(id = R.drawable.calendar),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dob.ifEmpty { "Date of birth" },
                        color = if (dob.isEmpty()) textGray else Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Error border overlay
                if (dobError != null) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .border(2.dp, Color.Red, RoundedCornerShape(48.dp))
                    )
                }
            }
            dobError?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // Gender and Profession Row (matching ProfileDetails exactly)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Gender
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = onGenderExpandedChange,
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(56.dp).menuAnchor()) {
                        Image(
                            painter = painterResource(id = R.drawable.text_box_bg),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                        )
                        TextField(
                            value = selectedGender.ifEmpty { "Gender" }.let { 
                                if (it == "Gender") it else it.replaceFirstChar { char -> 
                                    if (char.isLowerCase()) char.titlecase() else char.toString() 
                                }.replace("_", " ")
                            },
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            shape = RoundedCornerShape(48.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                focusedTrailingIconColor = lightGreen, unfocusedTrailingIconColor = lightGreen
                            ),
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { onGenderExpandedChange(false) },
                        modifier = Modifier.background(darkGreen)
                    ) {
                        genderOptions.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " "), color = Color.White) },
                                onClick = { 
                                    onGenderChange(item)
                                    onGenderExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }
            
            // Profession
            Box(modifier = Modifier.weight(1f)) {
                ExposedDropdownMenuBox(
                    expanded = professionExpanded,
                    onExpandedChange = onProfessionExpandedChange,
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
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionExpanded) },
                            shape = RoundedCornerShape(48.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                                focusedTrailingIconColor = lightGreen, unfocusedTrailingIconColor = lightGreen
                            ),
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = professionExpanded,
                        onDismissRequest = { onProfessionExpandedChange(false) },
                        modifier = Modifier.background(darkGreen)
                    ) {
                        professionOptions.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item, color = Color.White) },
                                onClick = { 
                                    onProfessionChange(item)
                                    onProfessionExpandedChange(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    // Note: Cannot preview with actual AuthViewModel in preview mode
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1C1E)
    ) {
        Text(
            text = "UserProfile Preview - Requires AuthViewModel",
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
    }
}
