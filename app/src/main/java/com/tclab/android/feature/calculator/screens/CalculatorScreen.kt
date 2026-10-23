
package com.tclab.android.feature.calculator.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tclab.android.feature.calculator.data.LabCalculations
import com.tclab.android.feature.calculator.data.UnitConverter
import java.util.Locale

private val modes = listOf(
    "Unit converter",
    "Solution dilution",
    "Molarity",
    "Mass concentration (g/L)",
    "Batch scaling",
    "Survival percentage"
)

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(modes[0]) }
    var menuOpen by remember { mutableStateOf(false) }

    var value by remember { mutableStateOf("1") }
    var category by remember { mutableStateOf("Mass") }
    var fromUnit by remember { mutableStateOf("g") }
    var toUnit by remember { mutableStateOf("mg") }
    var categoryOpen by remember { mutableStateOf(false) }
    var fromOpen by remember { mutableStateOf(false) }
    var toOpen by remember { mutableStateOf(false) }

    var c1 by remember { mutableStateOf("10") }
    var c2 by remember { mutableStateOf("2") }
    var finalVolume by remember { mutableStateOf("100") }
    var volumeUnit by remember { mutableStateOf("mL") }
    var volumeOpen by remember { mutableStateOf(false) }

    var mass by remember { mutableStateOf("5.844") }
    var molecularWeight by remember { mutableStateOf("58.44") }
    var molarityVolume by remember { mutableStateOf("1") }

    var concentrationMass by remember { mutableStateOf("5") }
    var concentrationVolume by remember { mutableStateOf("1") }

    var ingredientAmount by remember { mutableStateOf("5") }
    var oldBatch by remember { mutableStateOf("100") }
    var newBatch by remember { mutableStateOf("250") }

    var survived by remember { mutableStateOf("8") }
    var startingCount by remember { mutableStateOf("10") }

    var result by remember { mutableStateOf<String?>(null) }
    var explanation by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun clearResult() {
        result = null
        explanation = null
        error = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("TC Lab Calculator", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Laboratory calculations with visible units, formulas " +
                "and input validation."
        )

        OutlinedButton(
            onClick = { menuOpen = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Tool: $mode") }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            modes.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        mode = it
                        menuOpen = false
                        clearResult()
                    }
                )
            }
        }

        when (mode) {
            "Unit converter" -> {
                NumberField("Value", value) { value = it; clearResult() }

                Text("Unit category")
                OutlinedButton(
                    onClick = { categoryOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(category) }
                DropdownMenu(
                    expanded = categoryOpen,
                    onDismissRequest = { categoryOpen = false }
                ) {
                    UnitConverter.categories().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                val units = UnitConverter.unitsFor(item)
                                fromUnit = units.first()
                                toUnit = units.getOrElse(1) { units.first() }
                                categoryOpen = false
                                clearResult()
                            }
                        )
                    }
                }

                Text("Convert from")
                OutlinedButton(
                    onClick = { fromOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(fromUnit) }
                DropdownMenu(
                    expanded = fromOpen,
                    onDismissRequest = { fromOpen = false }
                ) {
                    UnitConverter.unitsFor(category).forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                fromUnit = item
                                fromOpen = false
                                clearResult()
                            }
                        )
                    }
                }

                Text("Convert to")
                OutlinedButton(
                    onClick = { toOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(toUnit) }
                DropdownMenu(
                    expanded = toOpen,
                    onDismissRequest = { toOpen = false }
                ) {
                    UnitConverter.unitsFor(category).forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                toUnit = item
                                toOpen = false
                                clearResult()
                            }
                        )
                    }
                }
            }

            "Solution dilution" -> {
                NumberField("Stock concentration C1", c1) {
                    c1 = it; clearResult()
                }
                NumberField("Target concentration C2", c2) {
                    c2 = it; clearResult()
                }
                Text(
                    "C1 and C2 must use the same concentration unit, " +
                        "such as both mM.",
                    style = MaterialTheme.typography.bodySmall
                )
                NumberField("Desired FINAL solution volume", finalVolume) {
                    finalVolume = it; clearResult()
                }
                OutlinedButton(
                    onClick = { volumeOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Volume unit: $volumeUnit") }
                DropdownMenu(
                    expanded = volumeOpen,
                    onDismissRequest = { volumeOpen = false }
                ) {
                    listOf("µL", "mL", "L").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                volumeUnit = item
                                volumeOpen = false
                                clearResult()
                            }
                        )
                    }
                }
                Text(
                    "Final volume is the total prepared solution volume, " +
                        "not automatically the amount of water to add.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            "Molarity" -> {
                NumberField("Solute mass (g)", mass) {
                    mass = it; clearResult()
                }
                NumberField("Molecular weight (g/mol)", molecularWeight) {
                    molecularWeight = it; clearResult()
                }
                NumberField("Final solution volume (L)", molarityVolume) {
                    molarityVolume = it; clearResult()
                }
                Text("Molarity = mass ÷ molecular weight ÷ final volume.")
            }

            "Mass concentration (g/L)" -> {
                NumberField("Solute mass (g)", concentrationMass) {
                    concentrationMass = it; clearResult()
                }
                NumberField("Final solution volume (L)", concentrationVolume) {
                    concentrationVolume = it; clearResult()
                }
                Text("Concentration = grams of solute ÷ final volume in litres.")
            }

            "Batch scaling" -> {
                NumberField("Original ingredient amount", ingredientAmount) {
                    ingredientAmount = it; clearResult()
                }
                NumberField("Original batch size", oldBatch) {
                    oldBatch = it; clearResult()
                }
                NumberField("New batch size", newBatch) {
                    newBatch = it; clearResult()
                }
                Text(
                    "Use the same unit for both batch sizes. " +
                        "The ingredient result retains its original unit."
                )
            }

            "Survival percentage" -> {
                NumberField("Surviving plants or samples", survived) {
                    survived = it; clearResult()
                }
                NumberField("Starting plants or samples", startingCount) {
                    startingCount = it; clearResult()
                }
                Text("Survival % = surviving count ÷ starting count × 100.")
            }
        }

        Button(
            onClick = {
                clearResult()
                try {
                    when (mode) {
                        "Unit converter" -> {
                            val input = value.number("Value")
                            val converted = UnitConverter.convert(
                                input, fromUnit, toUnit
                            )
                            result = "${fmt(converted)} $toUnit"
                            explanation = "$input $fromUnit converted to $toUnit."
                        }

                        "Solution dilution" -> {
                            val stock = c1.number("Stock concentration")
                            val target = c2.number("Target concentration")
                            val volume = finalVolume.number("Final volume")
                            val stockVolume = LabCalculations.dilutionVolume(
                                stock, target, volume
                            )
                            result = "${fmt(stockVolume)} $volumeUnit stock"
                            explanation =
                                "C1 × V1 = C2 × V2. Measure the stock volume, " +
                                "then bring the preparation to the specified " +
                                "total final volume. Do not assume the volume " +
                                "difference is always the exact water volume."
                        }

                        "Molarity" -> {
                            val answer = LabCalculations.molarity(
                                mass.number("Solute mass"),
                                molecularWeight.number("Molecular weight"),
                                molarityVolume.number("Final volume")
                            )
                            result = "${fmt(answer)} mol/L (M)"
                            explanation =
                                "Moles = mass ÷ molecular weight. " +
                                "Molarity = moles ÷ final volume in litres."
                        }

                        "Mass concentration (g/L)" -> {
                            val m = concentrationMass.number("Solute mass")
                            val v = concentrationVolume.number("Final volume")
                            require(m >= 0) { "Mass cannot be negative." }
                            require(v > 0) { "Final volume must be positive." }
                            result = "${fmt(m / v)} g/L"
                            explanation =
                                "Mass concentration uses solute mass and " +
                                "the final solution volume."
                        }

                        "Batch scaling" -> {
                            val answer = LabCalculations.scaleIngredient(
                                ingredientAmount.number("Ingredient amount"),
                                oldBatch.number("Original batch size"),
                                newBatch.number("New batch size")
                            )
                            result = fmt(answer)
                            explanation =
                                "New amount = original amount × new batch " +
                                "size ÷ original batch size. Result uses " +
                                "the original ingredient unit."
                        }

                        "Survival percentage" -> {
                            val alive = survived.number("Surviving count")
                            val start = startingCount.number("Starting count")
                            require(alive >= 0) {
                                "Surviving count cannot be negative."
                            }
                            require(start > 0) {
                                "Starting count must be greater than zero."
                            }
                            require(alive <= start) {
                                "Surviving count cannot exceed starting count."
                            }
                            result = "${fmt(alive / start * 100.0)}%"
                            explanation =
                                "Calculated from the counts entered; " +
                                "this is not an independently measured value."
                        }
                    }
                } catch (e: Exception) {
                    error = e.message ?: "Check the entered values."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cannot calculate: $error",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        if (result != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Calculated result", style = MaterialTheme.typography.titleMedium)
                    Text(result!!, style = MaterialTheme.typography.headlineSmall)
                    Text(explanation ?: "")
                }
            }
        }

        Text("Lab integrity", style = MaterialTheme.typography.titleMedium)
        Text(
            "Keep units attached to values. Distinguish w/v, v/v and w/w " +
                "percentages. Use measured pH, never a guessed value. " +
                "Do not invent missing protocol concentrations. " +
                "A calculated value is not a measured result.",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "This first version does not yet save calculation history or " +
                "attach results to plant batches, protocols or research records.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        )
    )
}

private fun String.number(label: String): Double {
    val parsed = trim().toDoubleOrNull()
        ?: throw IllegalArgumentException("$label must be a valid number.")
    require(parsed.isFinite()) { "$label must be a finite number." }
    return parsed
}

private fun fmt(value: Double): String =
    String.format(Locale.US, "%.6g", value)
