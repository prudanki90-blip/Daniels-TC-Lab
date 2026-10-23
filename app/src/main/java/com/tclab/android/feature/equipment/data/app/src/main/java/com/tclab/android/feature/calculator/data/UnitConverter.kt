package com.tclab.android.feature.calculator.data

data class UnitDefinition(
    val symbol: String,
    val category: String,
    val factorToBase: Double,
    val offsetToBase: Double = 0.0
) {
    fun toBase(value: Double) = value * factorToBase + offsetToBase
    fun fromBase(value: Double) = (value - offsetToBase) / factorToBase
}

object UnitConverter {
    private val units = listOf(
        UnitDefinition("µg", "Mass", 1e-9),
        UnitDefinition("mg", "Mass", 1e-6),
        UnitDefinition("g", "Mass", 1e-3),
        UnitDefinition("kg", "Mass", 1.0),

        UnitDefinition("µL", "Volume", 1e-6),
        UnitDefinition("mL", "Volume", 1e-3),
        UnitDefinition("L", "Volume", 1.0),

        UnitDefinition("µm", "Length", 1e-6),
        UnitDefinition("mm", "Length", 1e-3),
        UnitDefinition("cm", "Length", 1e-2),
        UnitDefinition("m", "Length", 1.0),

        UnitDefinition("s", "Time", 1.0),
        UnitDefinition("min", "Time", 60.0),
        UnitDefinition("h", "Time", 3600.0),
        UnitDefinition("day", "Time", 86400.0),

        UnitDefinition("Pa", "Pressure", 1.0),
        UnitDefinition("kPa", "Pressure", 1000.0),
        UnitDefinition("bar", "Pressure", 100000.0),
        UnitDefinition("atm", "Pressure", 101325.0),

        UnitDefinition("mg/mL", "Mass concentration", 1.0),
        UnitDefinition("g/L", "Mass concentration", 1.0),
        UnitDefinition("g/mL", "Mass concentration", 1000.0),

        UnitDefinition("M", "Molar concentration", 1.0),
        UnitDefinition("mM", "Molar concentration", 1e-3),
        UnitDefinition("µM", "Molar concentration", 1e-6),
        UnitDefinition("nM", "Molar concentration", 1e-9),

        UnitDefinition("K", "Temperature", 1.0),
        UnitDefinition("°C", "Temperature", 1.0, 273.15),
        UnitDefinition(
            "°F",
            "Temperature",
            5.0 / 9.0,
            273.15 - 32.0 * 5.0 / 9.0
        )
    )

    fun categories(): List<String> =
        units.map { it.category }.distinct()

    fun unitsFor(category: String): List<String> =
        units.filter { it.category == category }
            .map { it.symbol }

    fun convert(value: Double, from: String, to: String): Double {
        val source = units.firstOrNull { it.symbol == from }
            ?: error("Unknown source unit: $from")
        val target = units.firstOrNull { it.symbol == to }
            ?: error("Unknown target unit: $to")

        require(source.category == target.category) {
            "Units must belong to the same category."
        }

        return target.fromBase(source.toBase(value))
    }
}

object LabCalculations {

    /** C1 × V1 = C2 × V2. Concentrations must use the same unit. */
    fun dilutionVolume(
        stockConcentration: Double,
        targetConcentration: Double,
        finalVolume: Double
    ): Double {
        require(stockConcentration > 0.0)
        require(targetConcentration >= 0.0)
        require(finalVolume > 0.0)
        require(targetConcentration <= stockConcentration) {
            "Target concentration exceeds stock concentration."
        }

        return targetConcentration * finalVolume / stockConcentration
    }

    fun scaleIngredient(
        originalAmount: Double,
        originalBatchVolume: Double,
        newBatchVolume: Double
    ): Double {
        require(originalAmount >= 0.0)
        require(originalBatchVolume > 0.0)
        require(newBatchVolume > 0.0)

        return originalAmount * newBatchVolume / originalBatchVolume
    }

    fun molarity(
        massGrams: Double,
        molecularWeightGramsPerMole: Double,
        finalVolumeLitres: Double
    ): Double {
        require(massGrams >= 0.0)
        require(molecularWeightGramsPerMole > 0.0)
        require(finalVolumeLitres > 0.0)

        return massGrams /
            molecularWeightGramsPerMole /
            finalVolumeLitres
    }
}
