package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.appforcooking.domain.models.ShownRecipes
import com.example.appforcooking.presentation.data.FilterState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFilterScreen(
    navController: NavHostController
) {
    val currentFilters = FilterState.currentFilters

    var showEasy by remember { mutableStateOf(currentFilters.showEasy) }
    var showMid by remember { mutableStateOf(currentFilters.showMid) }
    var showHard by remember { mutableStateOf(currentFilters.showHard) }
    var hideAllergyRecipes by remember { mutableStateOf(currentFilters.hideAllergyRecipes) }

    var isApplying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Фильтр рецептов") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isApplying) navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3949AB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Сложность приготовления",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FilterChipWithCheckbox(
                selected = showEasy,
                onSelectedChange = { showEasy = it },
                label = "Легко",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterChipWithCheckbox(
                selected = showMid,
                onSelectedChange = { showMid = it },
                label = "Средне",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterChipWithCheckbox(
                selected = showHard,
                onSelectedChange = { showHard = it },
                label = "Сложно",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Divider(
                color = Color(0xFF3949AB).copy(alpha = 0.3f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Аллергии",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FilterChipWithCheckbox(
                selected = hideAllergyRecipes,
                onSelectedChange = { hideAllergyRecipes = it },
                label = "Показывать рецепты с аллергенами",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isApplying) {
                            showEasy = true
                            showMid = true
                            showHard = true
                            hideAllergyRecipes = true
                        }
                    },
                    enabled = !isApplying,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сбросить все")
                }

                Button(
                    onClick = {
                        if (!isApplying) {
                            isApplying = true
                            val newConfig = ShownRecipes(
                                showEasy = showEasy,
                                showMid = showMid,
                                showHard = showHard,
                                hideAllergyRecipes = hideAllergyRecipes
                            )
                            FilterState.currentFilters = newConfig
                            navController.popBackStack()
                        }
                    },
                    enabled = !isApplying,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3949AB)
                    )
                ) {
                    Text("Применить")
                }
            }
        }
    }
}

@Composable
fun FilterChipWithCheckbox(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF3949AB).copy(alpha = 0.2f),
            selectedLabelColor = Color(0xFF3949AB)
        ),
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF3949AB)
                )
            }
        } else null
    )
}