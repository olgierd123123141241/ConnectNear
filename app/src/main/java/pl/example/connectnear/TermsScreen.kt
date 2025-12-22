package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsScreen(onAcceptClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Regulamin Aplikacji",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        ) {
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ... (tu wstaw pełny regulamin)",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Justify
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAcceptClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Akceptuję i kontynuuję")
        }
    }
}
