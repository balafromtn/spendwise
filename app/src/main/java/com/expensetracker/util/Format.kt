package com.expensetracker.util

import java.text.NumberFormat
import java.util.Locale

object Format {
    private val inrNumber = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun inr(amount: Double): String = "\u20B9" + inrNumber.format(amount)
}
