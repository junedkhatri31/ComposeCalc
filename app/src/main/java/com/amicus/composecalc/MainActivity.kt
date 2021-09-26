package com.amicus.composecalc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amicus.composecalc.ui.theme.ComposeCalcTheme
import java.lang.Exception
import java.util.Stack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MyScreenContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    ComposeCalcTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}

fun preced(symbol: String): Int {
    return when(symbol) {
        "+", "-" -> 1
        "*", "/" -> 2
        else -> 0
    }
}

fun convertToPostfix(infix: List<String>): List<String> {
    val st = Stack<String>()
    st.push("#")
    val output = mutableListOf<String>()
    for (symbol in infix) {
        if (isNumeric(symbol)) {
            output.add(symbol)
        } else {
            if (preced(symbol) > preced(st.last())) {
                st.add(symbol)
            } else {
                while(st.last() != "#" && preced(symbol) <= preced(st.last())) {
                    output += st.last()
                    st.pop()
                }
                st.push(symbol)
            }
        }
    }
    while(st.last() != "#") {
        output += st.last()
        st.pop()
    }
    return output
}

fun resolvePostfix(postfix: List<String>): String {
    val st = Stack<String>()
    for (symbol in postfix) {
        if (!isOperationInside(symbol)) {
            st.push(symbol)
        } else {
            val a = st.pop().toInt()
            val b = st.pop().toInt()
            val resOne = calcOne(b, a, symbol)
            st.push(resOne.toString())
        }
    }
    return st.pop()
}

fun calcOne(a: Int, b: Int, s: String): Int {
    return when(s) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> a / b
        else -> throw Exception("Unsupported operation")
    }
}

fun calculate(context: Context, query: String): String {
    val splitRegex = Regex("(?<=[\\+\\-\\*\\/])|(?=[\\+\\-\\*\\/])")
    val splitted = query.split(splitRegex)
    val postfix = convertToPostfix(splitted)
    return resolvePostfix(postfix)
}

fun isOperationInside(symbol: String): Boolean {
    return "+" in symbol || "-" in symbol || "*" in symbol || "/" in symbol
}

fun isNumeric(symbol: String): Boolean {
    return try {
        symbol.toInt()
        true
    } catch (ex: NumberFormatException) {
        false
    }
}

fun canBeAdded(query: String, symbol: String): Boolean {
    if (isNumeric(symbol)) {
        return true
    } else {
        if (isOperationInside(symbol)) {
            if (query.isBlank()) {
                return false
            }
        }
        return true
    }
}

@Composable
fun MyScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        val context = LocalContext.current
        var screenText by remember {
            mutableStateOf("")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                screenText,
                fontSize = 32.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
        CalcGrid(
            listOf(
                listOf("1", "2", "3", "+"),
                listOf("4", "5", "6", "-"),
                listOf("7", "8", "9", "*"),
                listOf("C", "0", "<", "/"),
                listOf("="),
            ),
            onButtonClick = { buttonText: String ->
                when (buttonText) {
                    "C" -> screenText = ""
                    "<" -> {
                        if (screenText.isNotEmpty()) {
                            screenText = screenText.substring(0, screenText.length - 1)
                        }
                    }
                    "=" -> {
                        val result = calculate(context, screenText)
                        screenText = result
                    }
                    else -> {
                        if (canBeAdded(screenText, buttonText)) {
                            screenText += buttonText
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun CalcGrid(
    numbers: List<List<String>>,
    onButtonClick: (buttonText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        for (rowNumbers in numbers) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (colNumber in rowNumbers) {
                    Button(
                        onClick = {
                            onButtonClick(colNumber)
                        },
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f),
                        enabled = colNumber.isNotBlank()
                    ) {
                        Text(colNumber, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        MyScreenContent()
    }
}