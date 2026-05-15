package com.example.unitconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnitConverterApp()
        }
    }
}

data class Unit(val name: String, val factor: Double)

val categories = mapOf(
    "Length" to listOf(
        Unit("Meter", 1.0),
        Unit("Centimeter", 0.01),
        Unit("Kilometer", 1000.0),
        Unit("Inch", 0.0254),
        Unit("Foot", 0.3048),
        Unit("Mile", 1609.34)
    ),
    "Weight" to listOf(
        Unit("Kilogram", 1.0),
        Unit("Gram", 0.001),
        Unit("Pound", 0.453592),
        Unit("Ounce", 0.0283495),
        Unit("Ton", 1000.0)
    ),
    "Temperature" to listOf(
        Unit("Celsius", 1.0),
        Unit("Fahrenheit", 1.0),
        Unit("Kelvin", 1.0)
    ),
    "Volume" to listOf(
        Unit("Liter", 1.0),
        Unit("Milliliter", 0.001),
        Unit("US Gallon", 3.78541),
        Unit("US Cup", 0.236588)
    ),
    "Speed" to listOf(
        Unit("km/h", 1.0),
        Unit("mph", 1.60934),
        Unit("m/s", 3.6)
    )
)

@Composable
fun UnitConverterApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ConverterScreen()
        }
    }
}

@Composable
fun ConverterScreen() {
    var selectedCategory by remember { mutableStateOf("Length") }
    var fromUnit by remember { mutableStateOf(categories["Length"]!![0]) }
    var toUnit by remember { mutableStateOf(categories["Length"]!![1]) }
    var inputValue by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Unit Converter",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Category chips
        val categoryList = categories.keys.toList()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryList.forEach { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = {
                        selectedCategory = category
                        val units = categories[category]!!
                        fromUnit = units[0]
                        toUnit = units.getOrElse(1) { units[0] }
                        inputValue = ""
                    },
                    label = { Text(category) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        val currentUnits = categories[selectedCategory]!!

        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                inputValue = newValue.filter { it.isDigit() || it == '.' }
            },
            label = { Text("Value") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitDropdown(
                label = "From",
                units = currentUnits,
                selectedUnit = fromUnit,
                onUnitSelected = { fromUnit = it }
            )

            UnitDropdown(
                label = "To",
                units = currentUnits,
                selectedUnit = toUnit,
                onUnitSelected = { toUnit = it }
            )
        }

        Spacer(Modifier.height(32.dp))

        val result = calculateConversion(
            inputValue.toDoubleOrNull() ?: 0.0,
            fromUnit,
            toUnit,
            selectedCategory
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = result,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = toUnit.name,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UnitDropdown(
    label: String,
    units: List<Unit>,
    selectedUnit: Unit,
    onUnitSelected: (Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.weight(1f)
    ) {
        OutlinedTextField(
            value = selectedUnit.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun calculateConversion(value: Double, from: Unit, to: Unit, category: String): String {
    if (value == 0.0) return "0"

    return when (category) {
        "Temperature" -> {
            val celsius = when (from.name) {
                "Fahrenheit" -> (value - 32) * 5 / 9
                "Kelvin" -> value - 273.15
                else -> value
            }
            val result = when (to.name) {
                "Fahrenheit" -> celsius * 9 / 5 + 32
                "Kelvin" -> celsius + 273.15
                else -> celsius
            }
            round(result * 100) / 100
        }
        else -> {
            val baseValue = value * from.factor
            val result = baseValue / to.factor
            round(result * 100000) / 100000
        }
    }.toString()
}

@Preview(showBackground = true)
@Composable
fun PreviewConverter() {
    UnitConverterApp()
}