package pl.example.connectnear

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FifthStage(
    onNextClick: () -> Unit, // Akcja przejścia dalej
    onBackClick: () -> Unit  // Akcja powrotu
) {
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Główna zawartość - Wyśrodkowana
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "To jest piąty Ekran")
        }

        // Przycisk Ustawienia z zębatką - Wyśrodkowany w pionie, przy prawej krawędzi (10dp marginesu)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 10.dp), // 10 dp od prawej krawędzi
            contentAlignment = Alignment.CenterEnd // Wyśrodkowanie w pionie, po prawej stronie
        ) {
            Button(onClick = { showSettings = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ustawienia")
            }
        }

        // Nawigacja
        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text("Wróć")
        }
        Button(
            onClick = onNextClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Dalej")
        }

        // Dialog Ustawień
        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf("MENU") } // MENU, EMAIL, PASSWORD, DELETE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ustawienia") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (mode) {
                    "MENU" -> {
                        Button(onClick = { mode = "EMAIL" }, modifier = Modifier.fillMaxWidth()) {
                            Text("Zmiana e-mail")
                        }
                        Button(onClick = { mode = "PASSWORD" }, modifier = Modifier.fillMaxWidth()) {
                            Text("Resetowanie hasła")
                        }
                        Button(
                            onClick = { mode = "DELETE" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Usuń konto")
                        }
                    }
                    "EMAIL" -> ChangeEmailSection(
                        onSuccess = {
                            Toast.makeText(context, "Wysłano link weryfikacyjny na nowy e-mail.", Toast.LENGTH_LONG).show()
                            mode = "MENU"
                        },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                        onCancel = { mode = "MENU" }
                    )
                    "PASSWORD" -> ResetPasswordSection(
                        onSuccess = {
                            Toast.makeText(context, "Wysłano e-mail resetujący hasło.", Toast.LENGTH_LONG).show()
                            mode = "MENU"
                        },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                        onCancel = { mode = "MENU" }
                    )
                    "DELETE" -> DeleteAccountSection(
                        onSuccess = {
                            Toast.makeText(context, "Konto usunięte.", Toast.LENGTH_LONG).show()
                            onDismiss()
                            // Tutaj warto dodać logikę wylogowania/przejścia do ekranu startowego w nadrzędnym komponencie
                        },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
                        onCancel = { mode = "MENU" }
                    )
                }
            }
        },
        confirmButton = {
            if (mode == "MENU") {
                TextButton(onClick = onDismiss) {
                    Text("Zamknij")
                }
            }
        }
    )
}

@Composable
fun ChangeEmailSection(onSuccess: () -> Unit, onError: (String) -> Unit, onCancel: () -> Unit) {
    var newEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Podaj nowy e-mail i potwierdź hasłem:")
        OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text("Nowy e-mail") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Aktualne hasło") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Anuluj") }
            Button(onClick = {
                AuthRepo.updateUserEmail(newEmail, password, onSuccess, onError)
            }) {
                Text("Zmień")
            }
        }
    }
}

@Composable
fun ResetPasswordSection(onSuccess: () -> Unit, onError: (String) -> Unit, onCancel: () -> Unit) {
    val email = AuthRepo.currentUser?.email ?: ""

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Wyślij link resetujący hasło na adres:\n$email")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Anuluj") }
            Button(onClick = {
                AuthRepo.sendPasswordResetEmail(email, onSuccess, onError)
            }) {
                Text("Wyślij")
            }
        }
    }
}

@Composable
fun DeleteAccountSection(onSuccess: () -> Unit, onError: (String) -> Unit, onCancel: () -> Unit) {
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Aby usunąć konto, podaj hasło.\nTej operacji nie można cofnąć!", color = MaterialTheme.colorScheme.error)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onCancel) { Text("Anuluj") }
            Button(
                onClick = {
                    AuthRepo.deleteAccount(password, onSuccess, onError)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Usuń trwale")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FifthStagePreview() {
    FifthStage(onNextClick = {}, onBackClick = {})
}