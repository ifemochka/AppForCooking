package com.example.appforcooking.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.appforcooking.R
import com.example.appforcooking.data.auth.AuthManager
import com.example.appforcooking.data.local.database.CookingDatabase
import com.example.appforcooking.data.repositories.ProductRepository
import com.example.appforcooking.data.repositories.ServerRepository
import com.example.appforcooking.data.repositories.UserRepository
import com.example.appforcooking.domain.usecases.AddAllergyToUserUseCase
import com.example.appforcooking.domain.usecases.AddProductToPantryUseCase
import com.example.appforcooking.domain.usecases.GetUserAllergiesUseCase
import com.example.appforcooking.domain.usecases.GetUserProfileUseCase
import com.example.appforcooking.domain.usecases.RemoveAllergyFromUserUseCase
import com.example.appforcooking.domain.usecases.SearchProductsUseCase
import com.example.appforcooking.domain.usecases.UpdateUserProfileUseCase
import com.example.appforcooking.presentation.components.ProductItem
import com.example.appforcooking.presentation.components.ProductSearchItem
import com.example.appforcooking.presentation.viewmodels.AllergyViewModel
import com.example.appforcooking.presentation.viewmodels.ProfileViewModel
import com.example.appforcooking.presentation.viewmodels.SearchViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    val authManager = remember { AuthManager(context) }

    val keyboardController = LocalSoftwareKeyboardController.current

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val searchViewModel: SearchViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = CookingDatabase.getDatabase(context)
                val repository = ProductRepository(
                    database.productDao(),
                    database.pantryItemDao(),
                    database.allergyDao()
                )
                return SearchViewModel(
                    serverRepository = ServerRepository(),  // Добавляем
                    addProductToPantryUseCase = AddProductToPantryUseCase(repository),
                    addAllergyToUserUseCase = AddAllergyToUserUseCase(repository)
                ) as T
            }
        }
    )

    val allergyViewModel: AllergyViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val database = CookingDatabase.getDatabase(context)
                val repository = ProductRepository(
                    database.productDao(),
                    database.pantryItemDao(),
                    database.allergyDao()
                )
                return AllergyViewModel(
                    getUserAllergiesUseCase = GetUserAllergiesUseCase(repository),
                    removeAllergyFromUserUseCase = RemoveAllergyFromUserUseCase(repository)
                ) as T
            }
        }
    )

    val viewModel: ProfileViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(
                    authManager = authManager,
                    serverRepository = ServerRepository()
                ) as T
            }
        }
    )

    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val allergies by allergyViewModel.products.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editedFirstName by remember { mutableStateOf(userProfile?.firstName ?: "") }
    var editedLastName by remember { mutableStateOf(userProfile?.lastName ?: "") }

    val searchResults by remember { derivedStateOf { searchViewModel.searchResults } }
    val isSearchLoading by remember { derivedStateOf { searchViewModel.isLoading } }
    val searchError by remember { derivedStateOf { searchViewModel.error } }
    val successMessageSearch by remember { derivedStateOf { searchViewModel.successMessage } }

    LaunchedEffect(userProfile) {
        editedFirstName = userProfile?.firstName ?: ""
        editedLastName = userProfile?.lastName ?: ""
    }

    LaunchedEffect(successMessage, error) {
        if (successMessage != null || error != null) {
            delay(3000)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(successMessageSearch, searchError) {
        if (successMessageSearch!= null || searchError != null) {
            delay(3000)
            searchViewModel.clearMessages()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Фон",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.5f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            if (userProfile?.firstName != null || userProfile?.lastName != null) {
                Text(
                    text = "${userProfile?.firstName ?: ""} ${userProfile?.lastName ?: ""}".trim(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = userProfile?.email ?: "test@example.com",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Выйти из аккаунта")
            }
            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (successMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = successMessage!!,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isEditing) {
                        // Режим редактирования
                        OutlinedTextField(
                            value = editedFirstName,
                            onValueChange = { editedFirstName = it },
                            label = { Text("Имя") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editedLastName,
                            onValueChange = { editedLastName = it },
                            label = { Text("Фамилия") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text("Отмена")
                            }

                            Button(
                                onClick = {
                                    viewModel.updateUserProfile(
                                        firstName = editedFirstName.ifBlank { null },
                                        lastName = editedLastName.ifBlank { null }
                                    )
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Сохранить")
                            }
                        }
                    } else {
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Имя",
                            value = userProfile?.firstName ?: "Не указано"
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Фамилия",
                            value = userProfile?.lastName ?: "Не указано"
                        )

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = userProfile?.email ?: "test@example.com"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Редактировать профиль")
                        }
                    }



                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.navigate("cooking_history") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3949AB)
                        )
                    ) {
                        Text("История приготовлений")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Мои аллергии",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )

                        if (allergies.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${allergies.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            searchViewModel.onSearchQueryChanged(it)
                            isSearching = it.isNotEmpty()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Добавить аллегрии...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    searchViewModel.onSearchQueryChanged("")
                                    isSearching = false
                                    keyboardController?.hide()
                                }) {
                                    Text("×", fontSize = 24.sp)
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (searchError != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = searchError!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (successMessageSearch != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = successMessageSearch!!,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isSearching) {
                        // Показываем результаты поиска
                        if (isSearchLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (searchQuery.length >= 2 && searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ничего не найдено",
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(searchResults) { product ->
                                    ProductSearchItem(
                                        product = product,
                                        onAddClick = {
                                            searchViewModel.addAllergyToPantry(product)
                                            allergyViewModel.loadUserAllergies()
                                            keyboardController?.hide()
                                            searchQuery = ""
                                            isSearching = false
                                        }
                                    )
                                }
                            }
                        }
                    }


                    if (allergies.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "У вас нет аллергий",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Добавьте продукты, на которые у вас аллергия",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            items(allergies) { allergy ->
                                ProductItem(
                                    product = allergy,
                                    onDeleteClick = {
                                        allergyViewModel.removeAllergy(allergy)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
