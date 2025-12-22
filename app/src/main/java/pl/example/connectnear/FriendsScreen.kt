package pl.example.connectnear

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FriendsScreen(
    friendsViewModel: FriendsViewModel = viewModel()
) {
    val friends by friendsViewModel.friends.collectAsState()
    val friendSuggestions by friendsViewModel.friendSuggestions.collectAsState()
    val isLoading by friendsViewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "Sugestie znajomych",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                if (friendSuggestions.isEmpty()) {
                    item {
                        Text(
                            text = "Brak sugestii znajomych.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(friendSuggestions) { user ->
                        FriendSuggestionItem(user = user, friendsViewModel = friendsViewModel)
                    }
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Twoi znajomi",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (friends.isEmpty()) {
                    item {
                        Text(
                            text = "Nie masz jeszcze żadnych znajomych.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(friends) { user ->
                        FriendItem(user = user)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItem(user: FoundUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profileImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = user.name, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FriendSuggestionItem(user: FoundUser, friendsViewModel: FriendsViewModel) {
    val context = LocalContext.current
    val currentUserName by friendsViewModel.currentUserName.collectAsState()
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(user.profileImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(text = user.name, fontWeight = FontWeight.Bold)
                Text(text = "Znajomy z Facebooka", style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = {
            if (currentUser != null && currentUserName.isNotEmpty()) {
                FirebaseService.sendFriendRequest(
                    myUserId = currentUser.uid,
                    myName = currentUserName,
                    targetUserId = user.userId,
                    onSuccess = { Toast.makeText(context, "Wysłano zaproszenie!", Toast.LENGTH_SHORT).show() },
                    onError = { Toast.makeText(context, "Błąd: $it", Toast.LENGTH_SHORT).show() }
                )
            }
        }) {
            Icon(Icons.Default.Add, contentDescription = "Add friend")
        }
    }
}
