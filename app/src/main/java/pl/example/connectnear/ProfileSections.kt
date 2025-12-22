/*package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ProfileAvatarSection(
    imageUrl: String,
    isLoading: Boolean,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color.Gray, CircleShape)
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profilowe",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("Dodaj\nZdjęcie", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        if (isLoading) CircularProgressIndicator()
    }
    Text(
        text = "Kliknij w kółko, aby zmienić zdjęcie",
        color = Color.White.copy(0.8f),
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SocialMediaSection(
    instagram: String, onInstaChange: (String) -> Unit,
    facebook: String, onFbChange: (String) -> Unit,
    tiktok: String, onTiktokChange: (String) -> Unit,
    messenger: String, onMessengerChange: (String) -> Unit,
    youtube: String, onYoutubeChange: (String) -> Unit,
    youtubeTime: String, onTimeChange: (String) -> Unit
) {
    Text("Media Społecznościowe (Linki)", color = Color.White, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))

    CustomProfileTextField(value = instagram, onValueChange = onInstaChange, label = "Link do Instagrama")
    CustomProfileTextField(value = facebook, onValueChange = onFbChange, label = "Link do Facebooka")
    CustomProfileTextField(value = tiktok, onValueChange = onTiktokChange, label = "Link do TikToka")
    CustomProfileTextField(value = messenger, onValueChange = onMessengerChange, label = "Link do Messengera")

    Spacer(modifier = Modifier.height(10.dp))
    Text("Muzyka w tle", color = Color.White, fontWeight = FontWeight.Bold)

    CustomProfileTextField(value = youtube, onValueChange = onYoutubeChange, label = "Link do piosenki YouTube")

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Text("Zacznij od sekundy:", color = Color.White, modifier = Modifier.padding(end = 8.dp))
        CustomProfileTextField(
            value = youtubeTime,
            onValueChange = onTimeChange,
            label = "0",
            modifier = Modifier.width(100.dp)
        )
    }
}
*/