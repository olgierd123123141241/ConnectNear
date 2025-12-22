package pl.example.connectnear

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.example.connectnear.ui.theme.getCategoryGradient

data class AIMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
fun AIScreen(
    userCategory: String, // ZMIANA: Dodano kategorię, by ustawić tło
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf(
        AIMessage("Cześć! Jestem Twoim asystentem mapy w ConnectNear. Pomogę Ci odkrywać otoczenie i znajdować ciekawe osoby lub miejsca. W czym mogę Ci pomóc?", false)
    )) }
    var isTyping by remember { mutableStateOf(false) }

    fun sendMessage() {
        if (inputText.isBlank()) return
        
        val query = inputText
        val userMsg = AIMessage(query, true)
        messages = messages + userMsg
        inputText = ""
        isTyping = true

        scope.launch {
            val prompt = "Jesteś asystentem mapy w aplikacji społecznościowej ConnectNear. Twoim zadaniem jest pomoc w odnajdywaniu osób i miejsc na mapie w świecie rzeczywistym. Odpowiedz krótko i pomocnie na pytanie użytkownika: $query"
            
            val response = withContext(Dispatchers.IO) {
                AIRepo.generateContent(prompt)
            }
            
            if (response.startsWith("Przepraszamy")) { // Sprawdzanie po komunikacie błędu
                 // Wyświetl błąd, ale w przyjazny sposób
                 messages = messages + AIMessage("⚠️ Błąd AI: $response\nSprawdź połączenie lub konfigurację funkcji chmurowych.", false)
            } else {
                messages = messages + AIMessage(response, false)
            }
            isTyping = false
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Message", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Skopiowano do schowka", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // ZMIANA: Użycie gradientu zależnego od kategorii
            .background(getCategoryGradient(userCategory))
    ) {
        // Nagłówek
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(0.1f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Yellow)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ConnectNear AI", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Lista wiadomości
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages) { msg ->
                val align = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                val color = if (msg.isUser) Color(0xFF0AA4F4) else Color.DarkGray
                val textColor = Color.White
                
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!msg.isUser) {
                            IconButton(onClick = { copyToClipboard(msg.text) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopiuj", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = color),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 4.dp).widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = textColor,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        if (msg.isUser) {
                            IconButton(onClick = { copyToClipboard(msg.text) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopiuj", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
            if (isTyping) {
                item {
                    Text("AI pisze...", color = Color.White.copy(0.7f), fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        // Pole tekstowe
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.White, RoundedCornerShape(30.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Zapytaj AI...") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            IconButton(onClick = { sendMessage() }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Wyślij", tint = Color(0xFF0AA4F4))
            }
        }
    }
}
