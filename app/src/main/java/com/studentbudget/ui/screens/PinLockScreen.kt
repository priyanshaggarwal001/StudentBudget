package com.studentbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studentbudget.ui.theme.Black
import com.studentbudget.ui.theme.DarkGrey
import com.studentbudget.ui.theme.Danger
import com.studentbudget.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun PinLockScreen(
    correctPin: String,
    onUnlocked: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            if (enteredPin == correctPin) {
                delay(200)
                onUnlocked()
            } else {
                isError = true
                delay(400)
                enteredPin = ""
                isError = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔒", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enter PIN code",
            style = MaterialTheme.typography.titleLarge,
            color = White
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isError -> Danger
                                i < enteredPin.length -> White
                                else -> DarkGrey
                            }
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isError) "Incorrect PIN" else " ",
            color = if (isError) Danger else Color.Transparent, 
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        onClick = {
                            if (key == "DEL") {
                                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                            } else if (key.isNotEmpty() && enteredPin.length < 4) {
                                enteredPin += key
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun KeypadButton(key: String, onClick: () -> Unit) {
    if (key.isEmpty()) {
        Box(modifier = Modifier.size(72.dp))
        return
    }
    
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(DarkGrey)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (key == "DEL") {
            Text(
                "⌫",
                fontSize = 24.sp,
                color = White
            )
        } else {
            Text(
                text = key,
                style = MaterialTheme.typography.headlineLarge,
                color = White
            )
        }
    }
}
