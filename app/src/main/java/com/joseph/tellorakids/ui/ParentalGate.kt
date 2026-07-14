package com.joseph.tellorakids.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp

/**
 * A simple "Parental Gate" to ensure certain actions (like settings) 
 * are only performed by adults.
 */
@Composable
fun ParentalGate(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    // Generate a simple math problem
    val num1 = remember { (10..20).random() }
    val num2 = remember { (10..20).random() }
    val expectedResult = num1 + num2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parents Only", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("To access settings, please solve this:")
                Text("$num1 + $num2 = ?", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Answer") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answer.trim().toIntOrNull() == expectedResult) {
                        onSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SoftGreen)
            ) {
                Text("Verify", color = androidx.compose.ui.graphics.Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
