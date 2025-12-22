package pl.example.connectnear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TicTacToeGame(onClose: () -> Unit) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerX by remember { mutableStateOf(true) }
    val winner = calculateWinner(board)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kółko i Krzyżyk",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        val statusText = when {
            winner != null -> "Wygrywa: $winner"
            board.all { it.isNotEmpty() } -> "Remis!"
            else -> "Ruch gracza: ${if (isPlayerX) "X" else "O"}"
        }
        Text(text = statusText, color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Column {
            (0..2).forEach { row ->
                Row {
                    (0..2).forEach { col ->
                        val index = row * 3 + col
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .padding(4.dp)
                                .background(Color.Gray, shape = RoundedCornerShape(8.dp))
                                .clickable(enabled = board[index].isEmpty() && winner == null) {
                                    if (board[index].isEmpty() && winner == null) {
                                        val newBoard = board.toMutableList()
                                        newBoard[index] = if (isPlayerX) "X" else "O"
                                        board = newBoard
                                        isPlayerX = !isPlayerX
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = board[index],
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (board[index] == "X") Color(0xFFE91E63) else Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    board = List(9) { "" }
                    isPlayerX = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Reset")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Zamknij")
            }
        }
    }
}

private fun calculateWinner(board: List<String>): String? {
    val lines = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )
    for (line in lines) {
        val (a, b, c) = line
        if (board[a].isNotEmpty() && board[a] == board[b] && board[a] == board[c]) {
            return board[a]
        }
    }
    return null
}