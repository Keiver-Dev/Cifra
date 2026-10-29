package com.Vlynk.cifra.ui.tools

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode

enum class UnitCategory {
    LENGTH,
    WEIGHT,
    TEMPERATURE
}

data class UnitConverterUiState(
    val category: UnitCategory = UnitCategory.LENGTH,
    val inputValue: String = "1",
    val fromUnit: String = "km",
    val toUnit: String = "mi",
    val convertedValue: String = "0.6214",
    val equivalenceText: String = "1 km = 0.6214 mi"
)

class UnitConverterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    companion object {
        val lengthUnits = listOf("km", "m", "cm", "mi", "ft", "in")
        val weightUnits = listOf("kg", "g", "lb", "oz")
        val tempUnits = listOf("°C", "°F", "K")

        // Factors to base meter
        private val lengthToBaseMeter = mapOf(
            "km" to BigDecimal("1000"),
            "m" to BigDecimal("1"),
            "cm" to BigDecimal("0.01"),
            "mi" to BigDecimal("1609.344"),
            "ft" to BigDecimal("0.3048"),
            "in" to BigDecimal("0.0254")
        )

        // Factors to base gram
        private val weightToBaseGram = mapOf(
            "kg" to BigDecimal("1000"),
            "g" to BigDecimal("1"),
            "lb" to BigDecimal("453.59237"),
            "oz" to BigDecimal("28.349523")
        )
    }

    fun onCategorySelect(category: UnitCategory) {
        val (defaultFrom, defaultTo) = when (category) {
            UnitCategory.LENGTH -> "km" to "mi"
            UnitCategory.WEIGHT -> "kg" to "lb"
            UnitCategory.TEMPERATURE -> "°C" to "°F"
        }
        _uiState.update {
            it.copy(
                category = category,
                fromUnit = defaultFrom,
                toUnit = defaultTo
            )
        }
        recalculate()
    }

    fun onInputValueChange(value: String) {
        _uiState.update { it.copy(inputValue = value) }
        recalculate()
    }

    fun onFromUnitChange(unit: String) {
        _uiState.update { it.copy(fromUnit = unit) }
        recalculate()
    }

    fun onToUnitChange(unit: String) {
        _uiState.update { it.copy(toUnit = unit) }
        recalculate()
    }

    fun swapUnits() {
        _uiState.update {
            it.copy(
                fromUnit = it.toUnit,
                toUnit = it.fromUnit
            )
        }
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val rawInput = state.inputValue.replace(",", ".").trim()
        val num = rawInput.toBigDecimalOrNull()

        if (num == null) {
            _uiState.update { it.copy(convertedValue = "—", equivalenceText = "") }
            return
        }

        val converted = convert(num, state.fromUnit, state.toUnit, state.category)
        val oneEquiv = convert(BigDecimal.ONE, state.fromUnit, state.toUnit, state.category)

        _uiState.update {
            it.copy(
                convertedValue = formatResult(converted),
                equivalenceText = "1 ${state.fromUnit} = ${formatResult(oneEquiv)} ${state.toUnit}"
            )
        }
    }

    private fun convert(
        value: BigDecimal,
        from: String,
        to: String,
        category: UnitCategory
    ): BigDecimal {
        if (from == to) return value

        return when (category) {
            UnitCategory.LENGTH -> {
                val fromFactor = lengthToBaseMeter[from] ?: BigDecimal.ONE
                val toFactor = lengthToBaseMeter[to] ?: BigDecimal.ONE
                val base = value.multiply(fromFactor)
                base.divide(toFactor, 6, RoundingMode.HALF_UP).stripTrailingZeros()
            }
            UnitCategory.WEIGHT -> {
                val fromFactor = weightToBaseGram[from] ?: BigDecimal.ONE
                val toFactor = weightToBaseGram[to] ?: BigDecimal.ONE
                val base = value.multiply(fromFactor)
                base.divide(toFactor, 6, RoundingMode.HALF_UP).stripTrailingZeros()
            }
            UnitCategory.TEMPERATURE -> {
                convertTemperature(value.toDouble(), from, to).toBigDecimal()
            }
        }
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        // Convert from source to Celsius
        val celsius = when (from) {
            "°C" -> value
            "°F" -> (value - 32.0) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }
        // Convert from Celsius to target
        return when (to) {
            "°C" -> celsius
            "°F" -> (celsius * 9.0 / 5.0) + 32.0
            "K" -> celsius + 273.15
            else -> celsius
        }
    }

    private fun formatResult(bd: BigDecimal): String {
        val doubleVal = bd.toDouble()
        return if (doubleVal % 1.0 == 0.0 && doubleVal < 10000000 && doubleVal > -10000000) {
            "%.0f".format(doubleVal)
        } else {
            val rounded = bd.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()
            rounded.toPlainString()
        }
    }
}
