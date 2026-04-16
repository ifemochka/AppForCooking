package com.example.appforcooking.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appforcooking.domain.models.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecognitionResultDialog(
    recognizedText: String,
    uniqueProducts: Map<String, Product>,
    ambiguousProducts: Map<String, List<Product>>,
    notFound: List<String>,
    onAddProduct: (Product) -> Unit,
    onAddAllUnique: () -> Unit,
    onResolveAmbiguous: (String, List<Product>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Распознанные продукты", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Распознано:", fontSize = 12.sp, color = Color.Gray)
                        Text("\"$recognizedText\"", fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (uniqueProducts.isNotEmpty()) {
                    Text("Найденные продукты:", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.heightIn(max = 180.dp)) {
                        items(uniqueProducts.values.toList()) { product ->
                            VoiceProductItem(product = product, onAdd = { onAddProduct(product) })
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddAllUnique, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth()) {
                        Text("Добавить все (${uniqueProducts.size})")
                    }
                }

                if (ambiguousProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFFFF9800).copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                        Text("Некоторые продукты требуют уточнения", fontSize = 12.sp, color = Color(0xFFFF9800), modifier = Modifier.padding(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ambiguousProducts.isNotEmpty()) {
                    TextButton(onClick = { val first = ambiguousProducts.entries.first(); onResolveAmbiguous(first.key, first.value) }) {
                        Text("Уточнить (${ambiguousProducts.size})")
                    }
                }
                TextButton(onClick = onDismiss) { Text("Закрыть") }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbiguousProductDialog(
    productName: String,
    options: List<Product>,
    onSelect: (Product) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Уточните продукт", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(text = "Уточните продукт \"$productName\"?", modifier = Modifier.padding(bottom = 12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                    items(options) { product ->
                        Surface(modifier = Modifier.fillMaxWidth().clickable { onSelect(product) }, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = product.name, fontWeight = FontWeight.Medium)
                                    Text(text = "${product.category} • ${product.caloriesPer100g} ккал/100г", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSkip) { Text("Пропустить") }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        }
    )
}

@Composable
fun VoiceProductItem(product: Product, onAdd: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = product.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text(text = "${product.category} • ${product.caloriesPer100g} ккал/100г", fontSize = 11.sp, color = Color.Gray)
            }
            Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.height(32.dp)) {
                Text("+", fontSize = 14.sp)
            }
        }
    }
}