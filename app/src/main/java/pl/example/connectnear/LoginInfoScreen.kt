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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginInfoScreen(
    userSelection: UserSelection,
    onSaveSuccess: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(userSelection.name) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(Color(0xFF0AA4F4), Color(0xFF1CD9C3))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Jak się nazywasz?",
            fontSize = 28.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Text(
            text = "Ten nick musi być unikalny i będzie widoczny dla innych.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 30.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                if (errorMessage.isNotEmpty()) errorMessage = ""
                name = it
            },
            label = { Text("Twój Nick") },
            singleLine = true,
            isError = errorMessage.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.LightGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Button(
                onClick = {
                    val finalName = name.trim()

                    if (finalName.length < 3) {
                        errorMessage = "Nick musi mieć co najmniej 3 znaki."
                        return@Button
                    }

                    isLoading = true

                    FirebaseService.checkIfNameExists(finalName) { exists ->
                        if (exists) {
                            isLoading = false
                            errorMessage = "Ten nick jest już zajęty! Wybierz inny."
                        } else {
                            userSelection.name = finalName
                            userSelection.customId = finalName
                            FirebaseService.saveCurrentUser(userSelection, 
                                onSuccess = {
                                    isLoading = false
                                    onSaveSuccess()
                                },
                                onError = { errorMsg ->
                                    isLoading = false
                                    errorMessage = errorMsg
                                }
                            )
                        }
                    }
                },
                enabled = name.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006400))
            ) {
                Text("Dalej")
            }
        }
    }
}
