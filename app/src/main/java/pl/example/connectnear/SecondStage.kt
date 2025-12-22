package pl.example.connectnear

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.example.connectnear.ui.theme.getCategoryGradient

@Composable
fun SecondStage(
    onBackClick: () -> Unit,
    onNextClick: (UserSelection) -> Unit,
    initialUserSelection: UserSelection,
    modifier: Modifier = Modifier
) {
    var userSelection by remember { mutableStateOf(initialUserSelection) }

    val categories = listOf("Sport", "Nauka", "Impreza", "Randka", "Gry", "Podróże")
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            // ZMIANA: Użycie gradientu zależnego od kategorii
            .background(brush = getCategoryGradient(userSelection.category))
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cześć, ${userSelection.name}!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
        OutlinedTextField(
            value = userSelection.name,
            onValueChange = { userSelection = userSelection.copy(name = it) },
            label = { Text("Twój nick (możesz zmienić)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Co Cię interesuje?", fontSize = 20.sp, color = Color.Black, modifier = Modifier.padding(bottom = 10.dp))

        categories.forEach { category ->
            CategoryItem(category, userSelection.category == category) {
                userSelection = userSelection.copy(category = category)
            }
        }

        AnimatedVisibility(visible = userSelection.category == "Sport") {
            SportOptions(userSelection) { updatedSelection ->
                userSelection = updatedSelection
            }
        }

        AnimatedVisibility(visible = userSelection.category == "Nauka") {
            LearningOptions(userSelection) { updatedSelection ->
                userSelection = updatedSelection
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBackClick) { Text("Wróć") }
            Button(onClick = { onNextClick(userSelection) }) { Text("Dalej") }
        }
    }
}

@Composable
private fun CategoryItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).height(55.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFFA500) else Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text, fontSize = 20.sp, color = if (isSelected) Color.White else Color(0xFF0C5CBE), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SportOptions(userSelection: UserSelection, onUpdate: (UserSelection) -> Unit) {
    val sportModes = listOf("Partner do ćwiczeń", "Trener personalny")
    val sportLevels = listOf("Dla fanu", "Początkujący", "Średniozaawansowany", "Zaawansowany")

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Poziom:", color = Color.Black, fontWeight = FontWeight.Bold)
        sportLevels.forEach { level ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = userSelection.sportLevel == level, onClick = { onUpdate(userSelection.copy(sportLevel = level)) })
                Text(level, color = Color.Black)
            }
        }

        AnimatedVisibility(visible = userSelection.sportLevel.isNotEmpty()) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tryb:", color = Color.Black, fontWeight = FontWeight.Bold)
                sportModes.forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = userSelection.sportMode == mode, onClick = { onUpdate(userSelection.copy(sportMode = mode)) })
                        Text(mode, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningOptions(userSelection: UserSelection, onUpdate: (UserSelection) -> Unit) {
    val learningModes = listOf("Korepetycje", "Towarzysz do nauki", "Po prostu lubię się uczyć")
    val learningLevels = listOf("Szkoła podstawowa", "Szkoła średnia", "Studia", "Ogólny")

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Poziom:", color = Color.Black, fontWeight = FontWeight.Bold)
        learningLevels.forEach { level ->
             Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = userSelection.learningLevel == level, onClick = { onUpdate(userSelection.copy(learningLevel = level)) })
                Text(level, color = Color.Black)
            }
        }
        
        AnimatedVisibility(visible = userSelection.learningLevel.isNotEmpty()) {
            Column {
                 Spacer(modifier = Modifier.height(12.dp))
                Text("Tryb:", color = Color.Black, fontWeight = FontWeight.Bold)
                learningModes.forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = userSelection.learningMode == mode, onClick = { onUpdate(userSelection.copy(learningMode = mode)) })
                        Text(mode, color = Color.Black)
                    }
                }

                AnimatedVisibility(visible = userSelection.learningMode.isNotEmpty() && userSelection.learningMode != "Po prostu lubię się uczyć") {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = userSelection.subject,
                            onValueChange = { onUpdate(userSelection.copy(subject = it)) },
                            label = { Text("Przedmiot (np. Matematyka)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
