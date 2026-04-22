package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

    // Półprzezroczysty, zaokrąglony pasek zadań
    Surface(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.75f), // Prześwitywanie
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(70.dp)
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            items.forEach { item ->
                val isSelected = currentRoute == item.route
                
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (item.isAction) {
                            if (item.route == "radar_action") {
                                // Jeśli nie jesteśmy na mapie, najpierw tam idziemy
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
                            modifier = Modifier.size(26.dp),
                            tint = if (isSelected) Color(0xFF7B96FF) else Color.Gray
                        )
                    },
                    label = { 
                        Text(
                            text = item.label, 
                            fontSize = 10.sp,
                            color = if (isSelected) Color(0xFF7B96FF) else Color.Gray
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF7B96FF).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}
