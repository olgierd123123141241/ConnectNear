package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.example.connectnear.ui.theme.getCategoryGradient

@Composable
fun ThirdStage(
    onBackClick: () -> Unit,
    onNextClick: (String, String, String, String) -> Unit,
    initialMyAge: String = "",
    initialPreferredAge: String = "",
    initialMySex: String = "",
    initialPreferredSex: String = "",
    userCategory: String = "", // ZMIANA: Dodano kategorię, by móc dobrać kolor tła
    modifier: Modifier = Modifier
) {
    var myAge by rememberSaveable { mutableStateOf<String?>(initialMyAge.ifEmpty { null }) }
    var preferredAge by rememberSaveable { mutableStateOf<String?>(initialPreferredAge.ifEmpty { null }) }
    var mySex by rememberSaveable { mutableStateOf<String?>(initialMySex.ifEmpty { null }) }
    var preferredSex by rememberSaveable { mutableStateOf<String?>(initialPreferredSex.ifEmpty { null }) }


    // Dane do wyboru
    val myAgeOptions = listOf("18-25", "26-35", "36-45", "46-100", "Nie chcę podawać")
    val preferredAgeOptions = listOf("18-25", "26-35", "36-45", "46-100", "Bez znaczenia")
    val sexList = listOf("Kobieta", "Mężczyzna", "Nie chcę odpowiadać")
    val preferredSexList = listOf("Kobieta", "Mężczyzna", "Wszyscy")

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            // ZMIANA: Użycie gradientu zależnego od kategorii
            .background(brush = getCategoryGradient(userCategory))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // --- SEKCJA 1: TWÓJ WIEK ---
        Text(
            text = "Twój wiek:",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
        )

        myAgeOptions.forEach { ageRange ->
            SelectionCard(
                text = ageRange,
                isSelected = ageRange == myAge,
                onClick = { myAge = ageRange },
                selectedColor = Color(0xFF0400EE)
            )
        }

        // --- SEKCJA 2: JAKIEGO WIEKU SZUKASZ? ---
        Text(
            text = "Jakiego wieku szukasz?",
            fontSize = 20.sp,
            color = Color(0xFF0B6CDF),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 30.dp, bottom = 8.dp)
        )

        preferredAgeOptions.forEach { ageRange ->
            SelectionCard(
                text = ageRange,
                isSelected = ageRange == preferredAge,
                onClick = { preferredAge = ageRange },
                selectedColor = Color(0xFF6200EE)
            )
        }

        // --- SEKCJA 3: TWOJA PŁEĆ ---
        Text(
            text = "Twoja płeć:",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 30.dp, bottom = 8.dp)
        )

        sexList.forEach { sex ->
            SelectionCard(
                text = sex,
                isSelected = sex == mySex,
                onClick = { mySex = sex }
            )
        }

        // --- SEKCJA 4: KOGO SZUKASZ? ---
        Text(
            text = "Kogo szukasz?",
            fontSize = 20.sp,
            color = Color(0xFF0C5CBE),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 30.dp, bottom = 8.dp)
        )

        preferredSexList.forEach { sex ->
            SelectionCard(
                text = sex,
                isSelected =  sex == preferredSex,
                onClick = { preferredSex = sex },
                selectedColor = Color(0xFF6200EE)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- PRZYCISKI ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ,modifier = Modifier.height(50.dp)
            ) {
                Text("Wróć")
            }

            Button(
                onClick = {
                    if (myAge != null && preferredAge != null && mySex != null && preferredSex != null) {
                        onNextClick(myAge!!, preferredAge!!, mySex!!, preferredSex!!)
                    }
                },
                enabled = myAge != null && preferredAge != null && mySex != null && preferredSex != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF006400),
                    disabledContainerColor = Color.Gray
                ),
                modifier = Modifier.height(50.dp)
            ) {
                Text("Dalej")
            }
        }
    }
}

// Pomocniczy komponent do karty wyboru
@Composable
fun SelectionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = Color(0xFFFFA500)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(50.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 18.sp,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ThirdStagePreview() {
    ThirdStage(onBackClick = {}, onNextClick = { _, _, _, _ -> })
}
