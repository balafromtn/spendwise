package com.expensetracker.ui.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.data.local.entity.CategoryEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel = viewModel()
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var nameText by remember { mutableStateOf("") }

    val filteredCategories = categories.filter { it.type == selectedType }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Categories", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                nameText = ""
                viewModel.clearError()
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == "Expense",
                    onClick = { viewModel.selectType("Expense") },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = selectedType == "Income",
                    onClick = { viewModel.selectType("Income") },
                    label = { Text("Income") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredCategories, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (category.isDefault) "Default" else "Custom",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = {
                            nameText = category.name
                            viewModel.clearError()
                            editingCategory = category
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit ${category.name}")
                        }
                        IconButton(onClick = { deletingCategory = category }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete ${category.name}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        CategoryNameDialog(
            title = "Add Category",
            confirmLabel = "Add",
            nameText = nameText,
            onNameChange = { nameText = it },
            error = error,
            onDismiss = {
                viewModel.clearError()
                showAddDialog = false
            },
            onConfirm = {
                scope.launch {
                    val ok = viewModel.addCategory(nameText, selectedType)
                    if (ok) {
                        nameText = ""
                        showAddDialog = false
                    }
                }
            }
        )
    }

    // Edit dialog
    editingCategory?.let { category ->
        CategoryNameDialog(
            title = "Edit Category",
            confirmLabel = "Save",
            nameText = nameText,
            onNameChange = { nameText = it },
            error = error,
            onDismiss = {
                viewModel.clearError()
                editingCategory = null
            },
            onConfirm = {
                scope.launch {
                    val ok = viewModel.updateCategory(category, nameText)
                    if (ok) {
                        editingCategory = null
                    }
                }
            }
        )
    }

    // Delete confirmation
    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("Delete Category") },
            text = { Text("Delete \"${category.name}\"? Existing transactions using this category will be kept but will no longer appear in the category picker.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category)
                    deletingCategory = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    confirmLabel: String,
    nameText: String,
    onNameChange: (String) -> Unit,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { onNameChange(it) },
                    label = { Text("Category name") },
                    singleLine = true,
                    isError = error != null
                )
                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
