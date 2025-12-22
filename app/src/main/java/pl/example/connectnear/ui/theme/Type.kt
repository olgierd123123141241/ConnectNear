package pl.example.connectnear.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font

import pl.example.connectnear.R // Upewnij się, że to Twój pakiet R

// 1. Definiujemy rodzinę czcionek
val RoundedFontFamily = FontFamily(
    Font(R.font.quicksand_bold, FontWeight.Normal),
    Font(R.font.quicksand_regular, FontWeight.Bold)
)

// 2. Podmieniamy domyślne style w Typography
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = RoundedFontFamily, // <-- TU PRZYPISUJESZ NOWĄ CZCIONKĘ
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RoundedFontFamily, // <-- TU TEŻ
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    /* Możesz dodać też inne style, np. labelSmall, titleMedium itd. */
)

