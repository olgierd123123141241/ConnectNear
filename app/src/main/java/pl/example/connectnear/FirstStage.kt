package pl.example.connectnear

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FirstStage(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dodajemy scroll na wypadek małych ekranów
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0320C1),
                        Color(0xFF304FFE),
                        Color(0xFF0091EA),
                        Color(0xFF05A1F2),
                        Color(0xFF1CD9C3),
                        Color(0xFF00C853),
                        Color(0xFF64DD17),
                        Color(0xFFAEEA00),
                    )
                )
            )
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top, // Elementy od góry
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // --- LOGO ---
        // Upewnij się, że masz plik tlo.png lub tlo.xml w res/drawable
        Image(
            painter = painterResource(id = R.drawable.tlo),
            contentDescription = "Logo Connect Near",
            modifier = Modifier
                .size(250.dp)
                .padding(top = 40.dp, bottom = 20.dp)
        )

        // --- NAGŁÓWEK ---
        Text(
            text = "Witaj w Connect Near",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEBC609), // Żółty kolor dla kontrastu
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // --- OPIS (Skrócony i czytelny) ---
        Text(
            text = "Znajdź ludzi o podobnych celach w Twojej okolicy.\n\n" +
                    "Sport, nauka, impreza czy randka? Connect Near połączy Cię z osobami, które szukają tego samego.\n\n" +
                    "Prosto. Szybko. Skutecznie.",
            color = Color.White, // Biały tekst na ciemnym tle jest czytelniejszy!
            fontSize = 18.sp,
            lineHeight = 26.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 10.dp) // Ustawia lewo i prawo
                .padding(bottom = 40.dp)     // Dodaje dół
        )

        // --- PRZYCISK START ---
        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2), // Niebieski
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(horizontal = 20.dp),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text(
                text = "ROZPOCZNIJ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
