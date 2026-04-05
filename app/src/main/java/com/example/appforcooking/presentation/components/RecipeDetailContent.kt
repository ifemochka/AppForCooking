package com.example.appforcooking.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appforcooking.domain.models.Recipe
import com.example.appforcooking.domain.models.RecipeIngredient
import com.example.appforcooking.ui.theme.Fonts

@Composable
fun RecipeDetailContent(
    recipe: Recipe,
    ingredients: List<RecipeIngredient>,
    availableCount: Int,
    isAddingToShoppingList: Boolean = false,
    isAddingToHistory: Boolean = false,
    onAddToShoppingList: () -> Unit = {},
    onAddToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.LightGray)
        ) {
            if (recipe.imageUrl != null) {
                val imageResId = getImageResourceId(recipe.imageUrl)
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Картинки нет",
                            fontSize = 60.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Картинки нет",
                        fontSize = 60.sp
                    )
                }
            }

            Button(
                onClick = onAddToHistory,
                enabled = !isAddingToHistory,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                if (isAddingToHistory) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Готовлю")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3949AB),
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontFamily = Fonts.font
                )

                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp),
                        lineHeight = 24.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetailItem(
                        icon = Icons.Default.Timer,
                        title = "Время",
                        value = "${recipe.cookingTimeMinutes} мин",
                        color = Color(0xFF2196F3)
                    )

                    DetailItem(
                        icon = Icons.Default.Whatshot,
                        title = "Сложность",
                        value = recipe.difficulty,
                        color = Color(0xFF4CAF50)
                    )

                    DetailItem(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "Калории",
                        value = "${recipe.caloriesTotal} ккал",
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ингредиенты",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3949AB),
                        fontFamily = Fonts.font
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (availableCount == ingredients.size)
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$availableCount/${ingredients.size} в наличии",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (availableCount == ingredients.size)
                                Color(0xFF4CAF50)
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = availableCount.toFloat() / ingredients.size,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = if (availableCount == ingredients.size)
                        Color(0xFF4CAF50)
                    else
                        Color(0xFF3949AB),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ingredients.forEach { ingredient ->
                        RecipeIngredientItem(
                            ingredient = ingredient,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        val missingCount = ingredients.count { !it.isAvailable }
        if (missingCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Список покупок", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text("Не хватает $missingCount ингредиентов", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = onAddToShoppingList,
                        enabled = !isAddingToShoppingList,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        if (isAddingToShoppingList) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Добавить в список")
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Инструкции по приготовлению",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3949AB),
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontFamily = Fonts.font
                )

                val instructionsText = recipe.instructions
                val steps = if (instructionsText.contains("\\d+\\.".toRegex())) {
                    instructionsText.split("\\d+\\.".toRegex())
                        .filter { it.isNotBlank() }
                        .map { it.trim() }
                } else {
                    instructionsText.split("\n")
                        .filter { it.isNotBlank() }
                        .map { it.trim() }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        StepItem(
                            stepNumber = index + 1,
                            text = step
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(0xFF3949AB)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun getImageResourceId(imageName: String): Int {
    val context = LocalContext.current
    return try {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    } catch (e: Exception) {
        0
    }
}