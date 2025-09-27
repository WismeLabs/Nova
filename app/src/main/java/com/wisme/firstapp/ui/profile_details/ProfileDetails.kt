package com.wisme.firstapp.ui.profile_details

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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wisme.firstapp.R
import com.wisme.firstapp.data.local.NavigationState
import com.wisme.firstapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    authViewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedProfession by remember { mutableStateOf("") }
    var professionExpanded by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(R.drawable.avatar_1) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var professionError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    
    // Set date picker bounds: minimum age 13, maximum age 120
    val currentCalendar = java.util.Calendar.getInstance()
    val maxDateCalendar = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.YEAR, -13) // 13 years ago
    }
    val minDateCalendar = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.YEAR, -120) // 120 years ago
    }
    
    val datePickerState = rememberDatePickerState(
        yearRange = minDateCalendar.get(java.util.Calendar.YEAR)..maxDateCalendar.get(java.util.Calendar.YEAR)
    )
    
    // Validation functions
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }

    fun validateDisplayName(displayName: String): String? {
        return when {
            displayName.isBlank() -> "Display name is required"
            displayName.length < 2 -> "Display name must be at least 2 characters"
            else -> null
        }
    }

    fun validateDob(dob: String): String? {
        return when {
            dob.isBlank() -> "Date of birth is required"
            else -> {
                try {
                    // Parse the date (format: YYYY-MM-DD)
                    val parts = dob.split("-")
                    if (parts.size != 3) return "Invalid date format"
                    
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    
                    val selectedDate = java.util.Calendar.getInstance().apply {
                        set(year, month - 1, day) // Calendar month is 0-based
                    }
                    
                    val currentDate = java.util.Calendar.getInstance()
                    val age = currentDate.get(java.util.Calendar.YEAR) - selectedDate.get(java.util.Calendar.YEAR)
                    
                    // Check if birthday hasn't occurred this year yet
                    val adjustedAge = if (currentDate.get(java.util.Calendar.DAY_OF_YEAR) < selectedDate.get(java.util.Calendar.DAY_OF_YEAR)) {
                        age - 1
                    } else {
                        age
                    }
                    
                    when {
                        selectedDate.after(currentDate) -> "Date of birth cannot be in the future"
                        adjustedAge < 13 -> "You must be at least 13 years old to use this app"
                        adjustedAge > 120 -> "Please enter a valid date of birth"
                        else -> null
                    }
                } catch (e: Exception) {
                    "Please enter a valid date"
                }
            }
        }
    }

    fun validateGender(gender: String): String? {
        return when {
            gender.isBlank() -> "Please select your gender"
            else -> null
        }
    }

    fun validateProfession(profession: String): String? {
        return when {
            profession.isBlank() -> "Please select your profession"
            else -> null
        }
    }

    fun validateForm(): Boolean {
        nameError = validateName(name)
        displayNameError = validateDisplayName(displayName)
        dobError = validateDob(dob)
        genderError = validateGender(selectedGender)
        professionError = validateProfession(selectedProfession)
        return nameError == null && displayNameError == null && dobError == null && genderError == null && professionError == null
    }
    
    // Handle date selection
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = millis }
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val year = calendar.get(java.util.Calendar.YEAR)
            // Format as YYYY-MM-DD for backend compatibility
            val formattedDate = String.format("%04d-%02d-%02d", year, month, day)
            dob = formattedDate
            showDatePicker = false
            // Validate the selected date immediately
            dobError = validateDob(formattedDate)
        }
    }
    
    // Observe ViewModel state
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val navigationState by authViewModel.navigationState.collectAsStateWithLifecycle()
    
    // Handle navigation based on auth state
    LaunchedEffect(navigationState) {
        when (navigationState) {
            NavigationState.HOME -> onNavigateToHome()
            else -> { /* Stay on profile setup */ }
        }
    }
    
    // Clear gender error when selected
    LaunchedEffect(selectedGender) {
        if (selectedGender.isNotEmpty()) {
            genderError = null
        }
    }
    
    // Clear profession error when selected
    LaunchedEffect(selectedProfession) {
        if (selectedProfession.isNotEmpty()) {
            professionError = null
        }
    }
    
    // Error message will persist until user dismisses it manually

    val avatars = listOf(
        R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3,
        R.drawable.avatar_4, R.drawable.avatar_5
    )
    val lightGreen = Color(0xFFC1FF72)
    val textGray = Color.Gray
    val darkGreen = Color(0xFF1A241F)
    val Gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE4FFC2), Color(0xFFC1FF72))
    )

    Surface(
        color = Color.Black,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Profile Details",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))

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
                        onValueChange = { 
                            name = it
                            nameError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Your name", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                        leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                        shape = RoundedCornerShape(48.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
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

            Spacer(modifier = Modifier.height(16.dp))

            //DisplayName
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
                        onValueChange = { 
                            displayName = it
                            displayNameError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Display name", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                        leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                        shape = RoundedCornerShape(48.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
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

            Spacer(modifier = Modifier.height(16.dp))

            //Date of birth
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showDatePicker = true }
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
                        // Leading icon
                        Icon(
                            painter = painterResource(id = R.drawable.date), 
                            contentDescription = null, 
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Date text or placeholder
                        Text(
                            text = if (dob.isNotEmpty()) dob else "Date of Birth (DD/MM/YYYY)",
                            color = if (dob.isNotEmpty()) Color.White else textGray,
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
                            onDismissRequest = { genderExpanded = false },
                            modifier = Modifier.background(darkGreen)
                        ) {
                            listOf("male", "female", "other", "prefer_not_to_say").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " "), color = Color.White) },
                                    onClick = { selectedGender = item; genderExpanded = false; genderError = null }
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
                            onDismissRequest = { professionExpanded = false },
                            modifier = Modifier.background(darkGreen)
                        ) {
                            listOf("School Student", "UG Student", "PG Student", "PhD Scholar", "Working Professional", "Other").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Color.White) },
                                    onClick = { selectedProfession = item; professionExpanded = false; professionError = null }
                                )
                            }
                        }
                    }
                    professionError?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }
            
            // Add error displays for gender and profession at the end of the Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    genderError?.let { error ->
                        Text(
                            text = error,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    // Profession error already added above
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error Message Display
            errorMessage?.let { error ->
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
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Profile Setup Error",
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
                            onClick = { authViewModel.clearError() },
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    println("Complete Profile button clicked")
                    println("Form data - name: '$name', displayName: '$displayName', dob: '$dob', gender: '$selectedGender', profession: '$selectedProfession'")
                    
                    if (validateForm()) {
                        println("Form validation passed, calling completeProfile")
                        authViewModel.completeProfile(
                            name = name,
                            displayName = displayName,
                            dateOfBirth = dob,
                            gender = selectedGender,
                            profession = selectedProfession,
                            avatarId = when (selectedAvatar) {
                                R.drawable.avatar_1 -> 1
                                R.drawable.avatar_2 -> 2
                                R.drawable.avatar_3 -> 3
                                R.drawable.avatar_4 -> 4
                                R.drawable.avatar_5 -> 5
                                else -> 1
                            }
                        )
                    } else {
                        println("Form validation failed")
                        println("Validation errors:")
                        println("- nameError: $nameError")
                        println("- displayNameError: $displayNameError") 
                        println("- dobError: $dobError")
                        println("- genderError: $genderError")
                        println("- professionError: $professionError")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.height(49.dp),
                enabled = !isLoading
            ) {
                Box( //for the gradient background
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gradient, shape = RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Text("Saving...",style = MaterialTheme.typography.bodyMedium,fontSize = 18.sp,fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Complete Profile",style = MaterialTheme.typography.bodyMedium,fontSize = 18.sp,fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = { 
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                            val month = calendar.get(java.util.Calendar.MONTH) + 1
                            val year = calendar.get(java.util.Calendar.YEAR)
                            dob = String.format("%02d/%02d/%04d", day, month, year)
                            dobError = null
                        }
                        showDatePicker = false 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = lightGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = lightGreen
                    )
                ) {
                    Text("Cancel")
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = darkGreen,
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = lightGreen,
                subheadContentColor = Color.White,
                yearContentColor = Color.White,
                currentYearContentColor = lightGreen,
                selectedYearContentColor = Color.Black,
                selectedYearContainerColor = lightGreen,
                dayContentColor = Color.White,
                selectedDayContentColor = Color.Black,
                selectedDayContainerColor = lightGreen,
                todayContentColor = lightGreen,
                todayDateBorderColor = lightGreen,
                dayInSelectionRangeContainerColor = lightGreen.copy(alpha = 0.3f),
                dayInSelectionRangeContentColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = darkGreen,
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = lightGreen,
                    subheadContentColor = Color.White,
                    yearContentColor = Color.White,
                    currentYearContentColor = lightGreen,
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = lightGreen,
                    dayContentColor = Color.White,
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = lightGreen,
                    todayContentColor = lightGreen,
                    todayDateBorderColor = lightGreen,
                    dayInSelectionRangeContainerColor = lightGreen.copy(alpha = 0.3f),
                    dayInSelectionRangeContentColor = Color.White
                )
            )
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
    // Create a simple preview version without ViewModel dependencies
    ProfileDetailsScreenContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDetailsScreenContent() {
    var name by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var selectedProfession by remember { mutableStateOf("") }
    var professionExpanded by remember { mutableStateOf(false) }
    var selectedAvatar by remember { mutableStateOf(R.drawable.avatar_1) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    
    // Mock loading state for preview
    val isLoading = false
    
    // Validation functions
    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }

    fun validateDisplayName(displayName: String): String? {
        return when {
            displayName.isBlank() -> "Display name is required"
            displayName.length < 2 -> "Display name must be at least 2 characters"
            else -> null
        }
    }

    fun validateDob(dob: String): String? {
        return when {
            dob.isBlank() -> "Date of birth is required"
            else -> null
        }
    }

    fun validateForm(): Boolean {
        nameError = validateName(name)
        displayNameError = validateDisplayName(displayName)
        dobError = validateDob(dob)
        return nameError == null && displayNameError == null && dobError == null
    }

    val avatars = listOf(
        R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3,
        R.drawable.avatar_4, R.drawable.avatar_5
    )
    val lightGreen = Color(0xFFC1FF72)
    val textGray = Color.Gray
    val darkGreen = Color(0xFF1A241F)
    val Gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFE4FFC2), Color(0xFFC1FF72))
    )

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
            // Top bar with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Back",
                        tint = Color.Unspecified
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Profile Details",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                // Placeholder for symmetry
                Spacer(modifier = Modifier.size(48.dp))
            }
            
            Spacer(modifier = Modifier.height(40.dp))

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
                        onValueChange = { 
                            name = it
                            nameError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Your name", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                        leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                        shape = RoundedCornerShape(48.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
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

            Spacer(modifier = Modifier.height(16.dp))

            //DisplayName
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
                        onValueChange = { 
                            displayName = it
                            displayNameError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Display name", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                        leadingIcon = { Icon(painterResource(id = R.drawable.person), contentDescription = null, tint = Color.Unspecified) },
                        shape = RoundedCornerShape(48.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
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

            Spacer(modifier = Modifier.height(16.dp))

            //Date of birth
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.text_box_bg),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(RoundedCornerShape(48.dp))
                    )
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { 
                            dob = it
                            dobError = null // Clear error on input
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Date of Birth (DD/MM/YYYY)", color = textGray,style = MaterialTheme.typography.titleMedium,fontSize = 16.sp) },
                        leadingIcon = { Icon(painterResource(id = R.drawable.date), contentDescription = null, tint = Color.Unspecified) },
                        shape = RoundedCornerShape(48.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = lightGreen,
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = if (dobError != null) Color.Red else Color.Transparent, 
                            unfocusedIndicatorColor = if (dobError != null) Color.Red else Color.Transparent,
                        ),
                        singleLine = true,
                        isError = dobError != null
                    )
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
                                value = selectedGender.ifEmpty { "Gender" }.let { 
                                    if (it == "Gender") it else it.replaceFirstChar { char -> 
                                        if (char.isLowerCase()) char.titlecase() else char.toString() 
                                    }.replace("_", " ")
                                },
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
                            listOf("male", "female", "other", "prefer_not_to_say").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " "), color = Color.White) },
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
                            listOf("Student", "Software Engineer", "Teacher", "Doctor", "Business Analyst", "Designer", "Marketing", "Other").forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item, color = Color.White) },
                                    onClick = { selectedProfession = item; professionExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (validateForm()) {
                        // Mock action for preview
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.height(49.dp),
                enabled = !isLoading
            ) {
                Box( //for the gradient background
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gradient, shape = RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Text("Saving...",style = MaterialTheme.typography.bodyMedium,fontSize = 18.sp,fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Complete Profile",style = MaterialTheme.typography.bodyMedium,fontSize = 18.sp,fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
