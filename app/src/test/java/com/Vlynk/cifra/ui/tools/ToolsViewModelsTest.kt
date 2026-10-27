package com.Vlynk.cifra.ui.tools

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolsViewModelsTest {

    @Test
    fun tipSplit_calculatesCorrectValues() {
        val viewModel = TipSplitViewModel()
        viewModel.onTotalChange("100")
        viewModel.onTipPercentChange("15")
        viewModel.onPeopleChange("2")

        viewModel.calculate(
            errorInvalidMsg = "Invalid input",
            errorPeopleMsg = "Min 1 person"
        )

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertNotNull(state.perPerson)
        assertNotNull(state.tipTotal)
        assertNotNull(state.grandTotal)

        // 100 + 15 tip = 115 / 2 = 57.50
        assertTrue(state.perPerson!!.contains("57.50") || state.perPerson!!.contains("57,50"))
        assertTrue(state.tipTotal!!.contains("15.00") || state.tipTotal!!.contains("15,00"))
        assertTrue(state.grandTotal!!.contains("115.00") || state.grandTotal!!.contains("115,00"))
    }

    @Test
    fun tipSplit_invalidPeople_showsError() {
        val viewModel = TipSplitViewModel()
        viewModel.onTotalChange("100")
        viewModel.onTipPercentChange("10")
        viewModel.onPeopleChange("0")

        viewModel.calculate(
            errorInvalidMsg = "Invalid",
            errorPeopleMsg = "Must be at least 1 person"
        )

        val state = viewModel.uiState.value
        assertEquals("Must be at least 1 person", state.error)
        assertNull(state.perPerson)
    }

    @Test
    fun tipSplit_reset_clearsState() {
        val viewModel = TipSplitViewModel()
        viewModel.onTotalChange("50")
        viewModel.calculate("Err", "Err")
        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals("", state.total)
        assertNull(state.perPerson)
    }

    @Test
    fun discount_calculatesCorrectSavingsAndFinalPrice() {
        val viewModel = DiscountViewModel()
        viewModel.onPriceChange("200")
        viewModel.onDiscountPctChange("25")

        viewModel.calculate(
            errorInvalidMsg = "Invalid",
            errorRangeMsg = "Range 0-100"
        )

        val state = viewModel.uiState.value
        assertNull(state.error)
        // 200 - 25% = 150 final, 50 savings
        assertTrue(state.finalPrice!!.contains("150.00") || state.finalPrice!!.contains("150,00"))
        assertTrue(state.saving!!.contains("50.00") || state.saving!!.contains("50,00"))
    }

    @Test
    fun discount_invalidPercentage_showsRangeError() {
        val viewModel = DiscountViewModel()
        viewModel.onPriceChange("100")
        viewModel.onDiscountPctChange("150")

        viewModel.calculate(
            errorInvalidMsg = "Invalid",
            errorRangeMsg = "Must be between 0 and 100"
        )

        val state = viewModel.uiState.value
        assertEquals("Must be between 0 and 100", state.error)
        assertNull(state.finalPrice)
    }
}
