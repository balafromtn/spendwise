package com.expensetracker.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.domain.model.PaymentMethod
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    editTransactionId: Long? = null,
    viewModel: TransactionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(editTransactionId) {
        if (editTransactionId != null && editTransactionId > 0) {
            viewModel.loadForEdit(editTransactionId)
        } else {
            viewModel.resetForm()
        }
    }

    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            viewModel.clearSuccess()
            viewModel.resetForm()
            onBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = if (uiState.isEditing) "Edit Transaction" else "Add Transaction",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.type == "Expense",
                    onClick = { viewModel.updateType("Expense") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Expense") }
                SegmentedButton(
                    selected = uiState.type == "Income",
                    onClick = { viewModel.updateType("Income") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Income") }
            }

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.updateAmount(it) },
                label = { Text("Amount (\u20B9)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("\u20B9") }
            )

            // Category chips
            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = uiState.category == cat.name,
                        onClick = { viewModel.updateCategory(cat.name) },
                        label = { Text(cat.name) }
                    )
                }
            }

            // Date & Time with pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.date,
                    onValueChange = { viewModel.updateDate(it) },
                    label = { Text("Date") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val date = try {
                                LocalDate.parse(uiState.date, DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                            } catch (_: Exception) { LocalDate.now() }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val picked = LocalDate.of(year, month + 1, day)
                                    viewModel.updateDate(picked.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")))
                                },
                                date.year, date.monthValue - 1, date.dayOfMonth
                            ).show()
                        },
                    readOnly = false,
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Pick date",
                            modifier = Modifier.clickable {
                                val date = try {
                                    LocalDate.parse(uiState.date, DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
                                } catch (_: Exception) { LocalDate.now() }
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = LocalDate.of(year, month + 1, day)
                                        viewModel.updateDate(picked.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")))
                                    },
                                    date.year, date.monthValue - 1, date.dayOfMonth
                                ).show()
                            }
                        )
                    },
                    placeholder = { Text("dd-MMM-yyyy") }
                )
                OutlinedTextField(
                    value = uiState.time,
                    onValueChange = { viewModel.updateTime(it) },
                    label = { Text("Time") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val time = try {
                                LocalTime.parse(uiState.time, DateTimeFormatter.ofPattern("HH:mm"))
                            } catch (_: Exception) { LocalTime.now() }
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.updateTime(String.format("%02d:%02d", hour, minute))
                                },
                                time.hour, time.minute, true
                            ).show()
                        },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Pick time",
                            modifier = Modifier.clickable {
                                val time = try {
                                    LocalTime.parse(uiState.time, DateTimeFormatter.ofPattern("HH:mm"))
                                } catch (_: Exception) { LocalTime.now() }
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        viewModel.updateTime(String.format("%02d:%02d", hour, minute))
                                    },
                                    time.hour, time.minute, true
                                ).show()
                            }
                        )
                    },
                    placeholder = { Text("HH:mm") }
                )
            }

            // Payment method
            Text("Payment Method", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = uiState.paymentMethod == method.label,
                        onClick = { viewModel.updatePaymentMethod(method.label) },
                        label = { Text(method.label) }
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateNotes(it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Save button
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving
            ) {
                Text(
                    text = when {
                        uiState.isSaving -> "Saving..."
                        uiState.isEditing -> "Update"
                        else -> "Save"
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
