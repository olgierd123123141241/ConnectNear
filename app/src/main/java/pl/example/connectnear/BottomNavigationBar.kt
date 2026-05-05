package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val isAction: Boolean = false // Czy to jest akcja specjalna (np. Radar)
)

@Composable
fun BottomNavigationBar(navController: NavController, onRadarToggle: () -> Unit = {}) {
    val items = listOf(
        BottomNavItem("Mapy", Icons.Default.Map, "fourth_stage"),
        BottomNavItem("Blisko", Icons.Default.Radar, "radar_action", isAction = true),
        BottomNavItem("Czaty", Icons.Default.Chat, "friends_list_screen"),
        BottomNavItem("Grupy", Icons.Default.Groups, "groups_screen"),
        BottomNavItem("Profil", Icons.Default.Person, "profile_screen")
    )

    // Pasek zadań - podniesiony i z efektem "szkła"
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .navigationBarsPadding() // Podnosi pasek nad systemową linię nawigacji
            .padding(bottom = 12.dp) // Dodatkowy odstęp od krawędzi ekranu
            .clip(RoundedCornerShape(30.dp)),
        color = Color.White.copy(alpha = 0.88f), // Bardziej przejrzysty, ale wyraźny
        shadowElevation = 10.dp,
        tonalElevation = 5.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(80.dp) // Zwiększona wysokość, by napisy były czytelne
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                val isSelected = currentRoute == item.route
                
                NavigationBarItem(
                    selected = isSelected,
                    alwaysShowLabel = true, // Gwarantuje, że napisy pod ikonami są zawsze widoczne
                    onClick = {
                        if (item.isAction) {
                            if (item.route == "radar_action") {
                                if (currentRoute != "fourth_stage") {
                                    navController.navigate("fourth_stage")
                                }
                                onRadarToggle()
                            }
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon, 
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) Color(0xFF7B96FF) else Color(0xFF666666)
                        )
                    },
                    label = { 
                        Text(
                            text = item.label, 
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF7B96FF) else Color(0xFF333333)
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF7B96FF).copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
