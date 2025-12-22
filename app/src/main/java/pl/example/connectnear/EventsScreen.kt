package pl.example.connectnear

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    eventsViewModel: EventsViewModel = viewModel()
) {
    val events by eventsViewModel.events.collectAsState()
    val isLoading by eventsViewModel.isLoading.collectAsState()
    val selectedCategory by eventsViewModel.selectedCategory.collectAsState()
    val eventCategories = listOf("Wszystkie", "Sport", "Planszówki", "Kino/Teatr", "Spacer z psem", "Nauka/Warsztaty")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lokalne Wydarzenia") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(eventCategories) {
                    FilterChip(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        selected = it == (selectedCategory ?: "Wszystkie"),
                        onClick = { eventsViewModel.selectCategory(if (it == "Wszystkie") null else it) },
                        label = { Text(it) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(events) { event ->
                        EventItem(event = event, onJoinClick = {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                FirebaseService.joinFlashEvent(event.id, userId, 
                                    onSuccess = { /* TODO: Show success */ }, 
                                    onError = { /* TODO: Show error */ })
                            }
                        }, onEventClick = onEventClick)
                    }
                }
            }
        }
    }
}

@Composable
fun EventItem(
    event: FlashEvent,
    onJoinClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onEventClick(event.chatId) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, fontWeight = FontWeight.Bold)
            Text(text = event.description)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Kategoria: ${event.category}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Uczestnicy: ${event.participants.size}", style = MaterialTheme.typography.bodySmall)
            if (event.isRecurring) {
                Text(text = "Cykliczne: ${event.recurringDetails}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { 
                onJoinClick()
                Toast.makeText(context, "Dołączono do wydarzenia!", Toast.LENGTH_SHORT).show()
            }) {
                Text("Dołącz")
            }
        }
    }
}
