package com.example.marketlens.viewmodel

import com.example.marketlens.data.model.PortfolioHolding
import com.example.marketlens.data.model.PortfolioResult

data class PortfolioState(
    val isLoading:       Boolean              = false,
    val isSaving:        Boolean              = false,
    val isSimulating:    Boolean              = false,
    val errorMessage:    String?              = null,
    val holdings:        List<PortfolioHolding> = emptyList(),
    val result:          PortfolioResult?     = null,
    val simulationError: String?              = null,
    val selectedHorizon: PortfolioHorizon     = PortfolioHorizon.SIX_MONTHS,
    // Watchlist dropdown
    val watchlistSymbols: List<String>        = emptyList(),
    val formSymbol:       String              = "",
    val formShares:       String              = "",
    val formPurchasePrice: String             = "",
    val formError:        String?             = null,
    val showForm:         Boolean             = false,
    val isDropdownExpanded: Boolean           = false
)

enum class PortfolioHorizon(val label: String, val weeks: Int) {
    ONE_MONTH    ("1 Month",  4),
    THREE_MONTHS ("3 Months", 13),
    SIX_MONTHS   ("6 Months", 26),
    ONE_YEAR     ("1 Year",   52)
}
