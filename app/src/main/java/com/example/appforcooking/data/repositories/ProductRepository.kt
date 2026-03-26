package com.example.appforcooking.data.repositories

import com.example.appforcooking.data.local.database.dao.AllergyDao
import com.example.appforcooking.data.local.database.dao.PantryItemDao
import com.example.appforcooking.data.local.database.dao.ProductDao
import com.example.appforcooking.data.local.database.entities.AllergyEntity
import com.example.appforcooking.data.local.database.entities.PantryItemEntity
import com.example.appforcooking.data.local.database.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao,
    private val pantryItemDao: PantryItemDao,
    private val allergyDao: AllergyDao
)
{
    suspend fun insertProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    suspend fun insertAllProducts(products: List<ProductEntity>) {
        productDao.insertAllProducts(products)
    }

    fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    suspend fun deleteAllProducts() {
        productDao.deleteAllProducts()
    }

    fun getUserProducts(userId: Long): Flow<List<ProductEntity>> {
        return pantryItemDao.getUserProducts(userId)
    }

    fun getUserAllergies(userId: Long) : Flow<List<ProductEntity>> {
        return allergyDao.getUserAllergies(userId)
    }

    suspend fun getUserProductsByCategory(userId: Long, category: String): List<ProductEntity> {
        return pantryItemDao.getUserProductsByCategory(userId, category)
    }

    suspend fun addProductToUser(userId: Long, productId: Long) {
        val existingItem = pantryItemDao.getPantryItem(userId, productId)
        if (existingItem == null) {
        pantryItemDao.insertPantryItem(
            PantryItemEntity(
                userId = userId,
                productId = productId,
                isLow = false
            )
        )
            }
    }

    suspend fun addAllergyToUser(userId: Long, productId: Long) {
        val existingItem = allergyDao.getAllergy(userId, productId)
        if (existingItem == null) {
            allergyDao.insertAllergy(
                AllergyEntity(
                    userId = userId,
                    productId = productId
                )
            )
        }
    }


    suspend fun removeProductFromUser(userId: Long, productId: Long) {
        pantryItemDao.removeFromPantry(userId, productId)
    }

    suspend fun removeAllergyFromUser(userId: Long, productId: Long) {
        allergyDao.deleteAllergy(userId, productId)
    }

    // Для поиска всех продуктов в поисковой строке
    suspend fun searchProducts(query: String): List<ProductEntity> {
        return if (query.isBlank()) {
            emptyList()
        } else {
            productDao.searchProducts(query)
        }
    }

    suspend fun getProductById(productId: Long): ProductEntity? {
        return productDao.getProductById(productId)
    }
}
