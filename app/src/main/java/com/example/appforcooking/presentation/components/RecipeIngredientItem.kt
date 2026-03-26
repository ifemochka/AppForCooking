package com.example.appforcooking.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.appforcooking.domain.models.RecipeIngredient

@Composable
fun RecipeIngredientItem(
    ingredient: RecipeIngredient,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (ingredient.isAvailable)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.RemoveCircle,
                contentDescription = if (ingredient.isAvailable)
                    "Есть в холодильнике"
                else
                    "Нет в холодильнике",
                tint = if (ingredient.isAvailable)
                    Color(0xFF4CAF50)
                else
                    Color(0xFFF44336),
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = ingredient.productName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (ingredient.isAvailable)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Text(
            text = "${ingredient.quantity} ${ingredient.unit}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}