package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModernCalculatorTheme {
                ModernCalculator()
            }
        }
    }
}

data class CalculatorState(
    val mainDisplay: String = "0",
    val subDisplay: String = "",
    val operation: String? = null,
    val shouldClearOnNextNumber: Boolean = false
)

@Composable
fun ModernCalculator() {
    var state by remember { mutableStateOf(CalculatorState()) }

    fun handleInput(input: String) {
        state = when (input) {
            in "0".."9", "." -> {
                if (state.shouldClearOnNextNumber) {
                    state.copy(
                        mainDisplay = input,
                        shouldClearOnNextNumber = false
                    )
                } else {
                    if (input == "." && state.mainDisplay.contains(".")) {
                        state
                    } else {
                        state.copy(
                            mainDisplay = if (state.mainDisplay == "0" && input != ".")
                                input else state.mainDisplay + input
                        )
                    }
                }
            }
            "C" -> CalculatorState()
            "±" -> state.copy(
                mainDisplay = if (state.mainDisplay.startsWith("-"))
                    state.mainDisplay.substring(1) else "-${state.mainDisplay}"
            )
            "%" -> state.copy(
                mainDisplay = (state.mainDisplay.toDoubleOrNull()?.div(100) ?: 0.0).toString()
            )
            in arrayOf("+", "-", "×", "÷") -> {
                val currentNumber = state.mainDisplay
                state.copy(
                    subDisplay = "$currentNumber $input",
                    operation = input,
                    shouldClearOnNextNumber = true
                )
            }
            "=" -> {
                if (state.operation != null) {
                    val firstNumber = state.subDisplay.substringBefore(" ").toDoubleOrNull() ?: 0.0
                    val secondNumber = state.mainDisplay.toDoubleOrNull() ?: 0.0
                    val result = when (state.operation) {
                        "+" -> firstNumber + secondNumber
                        "-" -> firstNumber - secondNumber
                        "×" -> firstNumber * secondNumber
                        "÷" -> if (secondNumber != 0.0) firstNumber / secondNumber else Double.NaN
                        else -> secondNumber
                    }

                    state.copy(
                        mainDisplay = formatResult(result),
                        subDisplay = "",
                        operation = null,
                        shouldClearOnNextNumber = true
                    )
                } else state
            }
            else -> state
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF17181A))
            .padding(16.dp)
    ) {
        // Displays
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Text(
                text = state.subDisplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.End,
                fontSize = 24.sp,
                color = Color.Gray
            )
            Text(
                text = state.mainDisplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.End,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Botones
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF2D2F31))
                .padding(16.dp)
        ) {
            val buttons = listOf(
                listOf("C", "±", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { symbol ->
                        ModernCalculatorButton(
                            symbol = symbol,
                            modifier = if (symbol == "0")
                                Modifier.weight(2f) else Modifier.weight(1f),
                            onClick = { handleInput(symbol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernCalculatorButton(
    symbol: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val buttonColor = when (symbol) {
        "C" -> Color(0xFFFF5722)
        "±", "%" -> Color(0xFF9E9E9E)
        "÷", "×", "-", "+", "=" -> Color(0xFF2196F3)
        else -> Color(0xFF424242)
    }

    Box(
        modifier = modifier.padding(4.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .aspectRatio(if (symbol == "0") 2f else 1f)
                .fillMaxWidth(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = symbol,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatResult(number: Double): String {
    return when {
        number.isNaN() -> "Error"
        number == number.toLong().toDouble() -> number.toLong().toString()
        else -> "%.6f".format(number).trimEnd('0').trimEnd('.')
    }
}

@Composable
fun ModernCalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}