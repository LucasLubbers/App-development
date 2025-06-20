package com.example.workoutbuddyapplication.components

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MaterialDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var openDialog by remember { mutableStateOf(true) }

    if (openDialog) {
        LaunchedEffect(Unit) {
            val datePicker = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    openDialog = false
                    val picked = LocalDate.of(year, month + 1, dayOfMonth)
                    onDateSelected(picked.toString())
                },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            )
            datePicker.setOnCancelListener {
                openDialog = false
                onDismiss()
            }
            datePicker.show()
        }
    }
}