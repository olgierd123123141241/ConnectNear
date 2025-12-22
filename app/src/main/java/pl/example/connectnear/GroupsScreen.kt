package pl.example.connectnear

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.example.connectnear.ui.theme.getCategoryGradient

@Composable
fun GroupsScreen(
    myUserId: String,
    userCategory: String,
    onBackClick: () -> Unit,
    onGroupClick: (String, String) -> Unit,
    onPrivateChatClick: (String, String) -> Unit // Nowe: Callback do prywatnych czatów
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Moje, 1 = Szukaj
    var subTab by remember { mutableStateOf(0) } // Wewnątrz "Moje": 0 = Grupy, 1 = Czaty Prywatne

    var myGroups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var publicGroups by remember { mutableStateOf<List<Group>>(emptyList()) }
    var myPrivateChats by remember { mutableStateOf<List<FoundUser>>(emptyList()) } // Lista znajomych z którymi pisaliśmy (uproszczenie)
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRequestsDialog by remember { mutableStateOf<Group?>(null) }

    val context = LocalContext.current

    // Pobieranie danych
    LaunchedEffect(selectedTab, subTab) {
        if (selectedTab == 0) {
            if (subTab == 0) {
                FirebaseService.getMyGroups(myUserId) { myGroups = it }
            } else {
                // Pobieramy "znajomych" jako listę czatów prywatnych (uproszczenie, w pełnej wersji można pobrać listę aktywnych czatów)
                FirebaseService.getFriends(myUserId) { myPrivateChats = it }
            }
        } else {
            FirebaseService.getPublicGroups { allPublic ->
                publicGroups = allPublic.filter { !it.memberIds.contains(myUserId) }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getCategoryGradient(userCategory))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Nagłówek
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Text("Czaty i Grupy", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Główne Zakładki (Moje / Szukaj Grup)
            Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(0.2f), RoundedCornerShape(16.dp)).padding(4.dp)) {
                TabButton("Twoje Czaty", selectedTab == 0) { selectedTab = 0 }
                TabButton("Szukaj Grup", selectedTab == 1) { selectedTab = 1 }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pod-zakładki dla "Twoje Czaty"
            if (selectedTab == 0) {
                 Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { subTab = 0 }, colors = ButtonDefaults.textButtonColors(contentColor = if(subTab == 0) Color.White else Color.LightGray)) {
                        Text("Grupy", fontWeight = if(subTab==0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Text("|", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp))
                    TextButton(onClick = { subTab = 1 }, colors = ButtonDefaults.textButtonColors(contentColor = if(subTab == 1) Color.White else Color.LightGray)) {
                        Text("Prywatne", fontWeight = if(subTab==1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista
            LazyColumn(modifier = Modifier.padding(bottom = 60.dp)) {
                if (selectedTab == 0) {
                    if (subTab == 0) {
                        // --- MOJE GRUPY ---
                        if (myGroups.isEmpty()) item { Text("Nie należysz do żadnej grupy.", color = Color.White, modifier = Modifier.padding(8.dp)) }

                        items(myGroups) { group ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onGroupClick(group.groupId, group.name) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(group.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                        Spacer(modifier = Modifier.weight(1f))
                                        if (group.isPublic) Text("Publiczna", fontSize = 10.sp, color = Color.Blue) else Text("Prywatna", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(group.lastMessage, color = Color.Gray, fontSize = 14.sp, maxLines = 1)

                                    if (group.ownerId == myUserId && group.pendingRequests.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { showRequestsDialog = group },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                            modifier = Modifier.fillMaxWidth().height(35.dp)
                                        ) {
                                            Text("Oczekujące prośby: ${group.pendingRequests.size}")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // --- CZATY PRYWATNE ---
                        if (myPrivateChats.isEmpty()) item { Text("Brak aktywnych czatów.", color = Color.White, modifier = Modifier.padding(8.dp)) }
                        
                        items(myPrivateChats) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onPrivateChatClick(user.userId, user.name) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                } else {
                    // --- SZUKAJ PUBLICZNYCH ---
                    if (publicGroups.isEmpty()) item { Text("Brak nowych grup publicznych.", color = Color.White) }

                    items(publicGroups) { group ->
                        val isPending = group.pendingRequests.contains(myUserId)

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(group.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("Publiczna • ${group.memberIds.size} członków", fontSize = 12.sp, color = Color.Gray)
                                }

                                Button(
                                    onClick = {
                                        if (!isPending) {
                                            FirebaseService.requestJoinGroup(group.groupId, myUserId) {
                                                Toast.makeText(context, "Wysłano prośbę!", Toast.LENGTH_SHORT).show()
                                                selectedTab = 1 // Odśwież
                                            }
                                        }
                                    },
                                    enabled = !isPending,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isPending) Color.Gray else Color(0xFF00C853))
                                ) {
                                    Text(if (isPending) "Wysłano" else "Dołącz")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Przycisk "Wróć"
        Button(
            onClick = onBackClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("< Wróć")
        }

        // Przycisk dodawania (tylko w zakładce "Moje -> Grupy")
        if (selectedTab == 0 && subTab == 0) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Utwórz grupę")
            }
        }

        if (showCreateDialog) {
            CreateGroupDialog(myUserId, { showCreateDialog = false }, { showCreateDialog = false })
        }

        if (showRequestsDialog != null) {
            ManageRequestsDialog(
                group = showRequestsDialog!!,
                onDismiss = { showRequestsDialog = null },
                onProcessed = { showRequestsDialog = null }
            )
        }
    }
}

@Composable
fun RowScope.TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF006400) else Color.Transparent),
        elevation = null
    ) {
        Text(text, color = if (isSelected) Color.White else Color.Black)
    }
}

@Composable
fun CreateGroupDialog(myUserId: String, onDismiss: () -> Unit, onGroupCreated: () -> Unit) {
    var groupName by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) } // Wybór: Publiczna/Prywatna
    var myFriends by remember { mutableStateOf<List<FoundUser>>(emptyList()) }
    var selectedFriends by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseService.getFriends(myUserId) { myFriends = it; isLoading = false }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowa Grupa") },
        text = {
            Column {
                OutlinedTextField(value = groupName, onValueChange = { groupName = it }, label = { Text("Nazwa grupy") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))

                // Wybór typu
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isPublic = !isPublic }) {
                    Checkbox(checked = isPublic, onCheckedChange = { isPublic = it })
                    Text("Grupa Publiczna (Inni mogą prosić o dołączenie)")
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Zaproś znajomych:", fontWeight = FontWeight.Bold)

                if (isLoading) CircularProgressIndicator() else {
                    Box(modifier = Modifier.height(150.dp)) {
                        LazyColumn {
                            items(myFriends) { friend ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
                                    selectedFriends = if (selectedFriends.contains(friend.userId)) selectedFriends - friend.userId else selectedFriends + friend.userId
                                }) {
                                    Checkbox(checked = selectedFriends.contains(friend.userId), onCheckedChange = null)
                                    Text(friend.name)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (groupName.isNotBlank()) {
                    FirebaseService.createGroup(groupName, myUserId, selectedFriends.toList(), isPublic) { onGroupCreated() }
                }
            }, enabled = groupName.isNotBlank()) { Text("Utwórz") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Anuluj") } }
    )
}

@Composable
fun ManageRequestsDialog(group: Group, onDismiss: () -> Unit, onProcessed: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prośby o dołączenie") },
        text = {
            if (group.pendingRequests.isEmpty()) {
                Text("Brak oczekujących próśb.")
            } else {
                LazyColumn {
                    items(group.pendingRequests) { userId ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null)
                            Text("Osoba ID: ...${userId.takeLast(4)}")

                            Row {
                                IconButton(onClick = {
                                    FirebaseService.approveRequest(group.groupId, userId) { onProcessed() }
                                }) { Icon(Icons.Default.Check, null, tint = Color.Green) }

                                IconButton(onClick = {
                                    FirebaseService.rejectRequest(group.groupId, userId) { onProcessed() }
                                }) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Zamknij") }
        }
    )
}
