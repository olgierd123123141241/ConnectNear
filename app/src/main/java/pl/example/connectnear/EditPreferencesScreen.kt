package pl.example.connectnear

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pl.example.connectnear.ui.theme.getCategoryGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPreferencesScreen(
    userCategory: String, // ZMIANA: Dodano kategorię do tła
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<UserSelection?>(null) }

    var myAge by remember { mutableStateOf("") }
    var mySex by remember { mutableStateOf("") }

    val myAgeOptions = listOf("18-25", "26-35", "36-45", "46-100", "Nie chcę podawać")
    val sexList = listOf("Kobieta", "Mężczyzna", "Nie chcę odpowiadać")

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUserProfile { loadedUser ->
            user = loadedUser
            myAge = loadedUser?.myAge ?: ""
            mySex = loadedUser?.mySex ?: ""
        }
    }

    // Automatyczne zapisywanie z opóźnieniem
    LaunchedEffect(myAge, mySex) {
        if (user != null) { 
            delay(1500) // Czekaj 1.5 sekundy
            user?.let {
                val updatedUser = it.copy(
                    myAge = myAge,
                    mySex = mySex
                )
                FirebaseService.updateFullProfile(updatedUser, 
                    onSuccess = { onSaveSuccess() },
                    onError = { Toast.makeText(context, "Błąd zapisu", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(getCategoryGradient(userCategory))
    ) {
        Scaffold(
            containerColor = Color.Transparent, // Transparentne, aby tło Boxa było widoczne
            topBar = {
                TopAppBar(
                    title = { Text("Edytuj Preferencje") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, "Wróć")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, 
                        titleContentColor = Color.White, // Biały tekst dla lepszej czytelności na gradiencie
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) {
            if (user == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // SEKCJA 1: TWÓJ WIEK
                    Text("Twój wiek:", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    myAgeOptions.forEach { ageRange ->
                        SelectionCard(
                            text = ageRange, 
                            isSelected = myAge == ageRange,
                            onClick = { myAge = ageRange } 
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // SEKCJA 2: TWOJA PŁEĆ
                    Text("Twoja płeć:", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    sexList.forEach { sex ->
                        SelectionCard(
                            text = sex,
                            isSelected = mySex == sex,
                            onClick = { mySex = sex }
                        )
                    }
                }
            }
        }
    }
}
