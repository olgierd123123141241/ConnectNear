package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    // Przekazujemy Imię i ID dalej
    onLoginSuccess: (String, String) -> Unit,
    initialName: String = "",
    initialCustomId: String = ""
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var customId by rememberSaveable { mutableStateOf(initialCustomId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0AA4F4),
                        Color(0xFF1CD9C3)
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Wyśrodkowanie w pionie
    ) {
        Text(
            text = "Witaj w ConnectNear!",
            fontSize = 32.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Text(
            text = "Podaj swoje dane, aby inni mogli Cię znaleźć.",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Pole Imię
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Twój Nick / Imię") },
            singleLine = true,
            colors = textFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pole ID
        OutlinedTextField(
            value = customId,
            onValueChange = { customId = it },
            label = { Text("Unikalne ID (np. janek99)") },
            singleLine = true,
            colors = textFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && customId.isNotBlank()) {
                    onLoginSuccess(name, customId)
                }
            },
            enabled = name.isNotBlank() && customId.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006400)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Rozpocznij", fontSize = 18.sp)
        }
    }
}

// Pomocnicze kolory pól (skopiowane z poprzedniego kodu dla spójności)
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White.copy(alpha = 0.2f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.LightGray,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.LightGray
)
