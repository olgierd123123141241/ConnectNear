package pl.example.connectnear

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun StatusChip(id: String, text: String, currentStatus: String, onSelect: (String) -> Unit) {
    val isSelected = currentStatus == id
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(if (isSelected) "" else id) },
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color.White,
            selectedLabelColor = Color.Black,
            containerColor = Color.White.copy(0.1f),
            labelColor = Color.White
        ),
        border = BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(0.5f))
    )
}

@Composable
fun TabButton(icon: ImageVector, text: String, isSelected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (isSelected) 1.1f else 1f)
    val color = if (isSelected) Color.White else Color.White.copy(0.6f)
    
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(12.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = text, tint = color, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, color = color, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun SettingsButton(text: String, icon: ImageVector, color: Color = Color.White, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(0.15f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = color, fontSize = 16.sp)
        }
    }
}

// NAPRAWIONA WERSJA
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
            .background(Color.Gray.copy(alpha = 0.3f)) // Tło dla pustego awatara
            .border(2.dp, Color.White, CircleShape)
            .clickable { onImageClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Zdjęcie profilowe",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.AddAPhoto, contentDescription = "Dodaj zdjęcie", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
        }

        if (isLoading) {
             Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
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
    spotify: String, onSpotifyChange: (String) -> Unit,
    steam: String, onSteamChange: (String) -> Unit,
) {
    Text("Media Społecznościowe (Nazwy/Linki)", color = Color.White, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(10.dp))

    CustomProfileTextField(value = instagram, onValueChange = onInstaChange, label = "Instagram")
    CustomProfileTextField(value = facebook, onValueChange = onFbChange, label = "Facebook")
    CustomProfileTextField(value = tiktok, onValueChange = onTiktokChange, label = "TikTok")
    CustomProfileTextField(value = messenger, onValueChange = onMessengerChange, label = "Messenger")
    
    Spacer(modifier = Modifier.height(16.dp))
    Text("Integracje", color = Color.White, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    CustomProfileTextField(value = spotify, onValueChange = onSpotifyChange, label = "Link do Spotify (Playlista/Profil)")
    CustomProfileTextField(value = steam, onValueChange = onSteamChange, label = "Link do Steam / Friend Code")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestTagEditor(
    currentInterests: String,
    onInterestsChange: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val interestsList = remember(currentInterests) {
        currentInterests.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    
    val categories = mapOf(
        "Sport i Aktywność" to listOf("Siłownia", "Bieganie", "Rower", "Pływanie", "Joga", "Piłka nożna", "Koszykówka", "Tenis", "Siatkówka", "Sztuki walki", "Wspinaczka", "Rolki", "Taniec"),
        "Media, Filmy i Gry" to listOf("Muzyka", "Filmy", "Seriale", "Gry", "Gry planszowe", "E-sport", "Anime", "Książki", "Komiksy", "Podcasty", "YouTube"),
        "Imprezy i Rozrywka" to listOf("Kluby", "Imprezy", "Picie alkoholu", "Karaoke", "Koncerty", "Festiwale", "Puby", "Bilard", "Kręgle"),
        "Twórczość i Pasje" to listOf("Fotografia", "Rysowanie", "Pisanie", "Gotowanie", "DIY (Zrób to sam)", "Gra na instrumencie", "Śpiew", "Moda", "Makijaż"),
        "Wiedza i Styl Życia" to listOf("Podróże", "Samochody", "Technologia", "Zwierzęta", "Natura", "Polityka", "Historia", "Nauka", "Programowanie", "Wolontariat", "Psychologia", "Medytacja", "Astrologia")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Twoje zainteresowania:", color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            interestsList.forEach { interest ->
                InputChip(
                    selected = true,
                    onClick = { onInterestsChange((interestsList - interest).joinToString(",")) },
                    label = { Text(interest) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Usuń", modifier = Modifier.size(16.dp)) },
                    colors = InputChipDefaults.inputChipColors(selectedContainerColor = Color.White, selectedLabelColor = Color.Black),
                    border = null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Wpisz własne...", color = Color.White.copy(0.7f)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(0.5f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White),
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = {
                            if (text.isNotBlank() && !interestsList.contains(text.trim())) {
                                onInterestsChange((interestsList + text.trim()).joinToString(","))
                                text = ""
                            }
                        }) { Icon(Icons.Default.Add, contentDescription = "Dodaj", tint = Color.White) }
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))        

        categories.forEach { (category, tags) ->
            Text(text = category, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    val isSelected = interestsList.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onInterestsChange(if (isSelected) (interestsList - tag).joinToString(",") else (interestsList + tag).joinToString(",")) },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color.White, selectedLabelColor = Color.Black, containerColor = Color.White.copy(0.1f), labelColor = Color.White),
                        border = BorderStroke(1.dp, if (isSelected) Color.White else Color.White.copy(0.3f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(0.7f)) },
        modifier = modifier.fillMaxWidth(),
        minLines = minLines,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(0.5f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White)
    )
}
