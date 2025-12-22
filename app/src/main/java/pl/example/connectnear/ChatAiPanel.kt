package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSuggestionsPanel(
    isVisible: Boolean,
    isLoading: Boolean,
    suggestions: List<String>,
    selectedTone: String,
    onToneChange: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val tones = listOf("Popraw", "Formalny", "Luźny", "Zabawny", "Poetycki")

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Asystent AI ✨", fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Zamknij")
                }
            }
            Spacer(Modifier.height(8.dp))

            Text("Wybierz ton wypowiedzi:", fontSize = 12.sp, color = Color.Gray)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tones) {
                    tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = { onToneChange(tone) },
                        label = { Text(tone) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (suggestions.isNotEmpty()) {
                suggestions.forEach { suggestion ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { onSuggestionClick(suggestion) }
                    ) {
                        Text(suggestion, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onGenerateClick, modifier = Modifier.fillMaxWidth(), enabled = !isLoading) {
                Text("Generuj nowe sugestie")
            }
        }
    }
}
