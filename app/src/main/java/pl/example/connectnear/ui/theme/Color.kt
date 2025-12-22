package pl.example.connectnear.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ZMIANA: Nowa funkcja zwracająca główny kolor kategorii
fun getCategoryPrimaryColor(category: String): Color {
    return when (category) {
        "Randka" -> Color(0xFFB71C1C) // Ciemna czerwień
        "Sport" -> Color(0xFFEF6C00)   // Pomarańczowy
        "Nauka" -> Color(0xFF2E7D32)   // Zielony
        "Impreza" -> Color(0xFF7B1FA2) // Fioletowy
        "Gry", "Wspólna gra" -> Color(0xFF283593) // Granatowy
        "Podróże" -> Color(0xFFFF8F00) // Bursztynowy
        "Spacer" -> Color(0xFF558B2F)  // Jasny zielony
        "Kawa" -> Color(0xFF4E342E)    // Brązowy
        else -> Color(0xFFE3F2FD) // Domyślny jasny niebieski
    }
}

fun getCategoryGradient(category: String): Brush {
    // Stały niebieski kolor (zawsze na dole/końcu gradientu)
    val baseBlue = Color(0xFF0AA4F4)
    // Pobranie koloru głównego dla kategorii
    val categoryColor = getCategoryPrimaryColor(category)

    // Gradient jest teraz bezpośrednim przejściem z koloru kategorii do niebieskiego.
    return Brush.verticalGradient(
        colors = listOf(categoryColor, baseBlue)
    )
}
