package com.example.appforcooking.data.local.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appforcooking.data.local.database.dao.AllergyDao
import com.example.appforcooking.data.local.database.dao.PantryItemDao
import com.example.appforcooking.data.local.database.dao.ProductDao
import com.example.appforcooking.data.local.database.dao.RecipeDao
import com.example.appforcooking.data.local.database.dao.ShoppingListDao
import com.example.appforcooking.data.local.database.dao.UserDao
import com.example.appforcooking.data.local.database.dao.UserProfileDao
import com.example.appforcooking.data.local.database.entities.AllergyEntity
import com.example.appforcooking.data.local.database.entities.PantryItemEntity
import com.example.appforcooking.data.local.database.entities.ProductEntity
import com.example.appforcooking.data.local.database.entities.RecipeEntity
import com.example.appforcooking.data.local.database.entities.RecipeIngredientEntity
import com.example.appforcooking.data.local.database.entities.ShoppingListItemEntity
import com.example.appforcooking.data.local.database.entities.UserEntity
import com.example.appforcooking.data.local.database.entities.UserProfileEntity

@Database(
    entities = [
        ProductEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        UserEntity::class,
        UserProfileEntity::class,
        PantryItemEntity::class,
        AllergyEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CookingDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao

    abstract fun userProfileDao(): UserProfileDao
    abstract fun pantryItemDao(): PantryItemDao
    abstract fun allergyDao(): AllergyDao

    abstract fun shoppingListDao(): ShoppingListDao


    companion object {
        @Volatile
        private var INSTANCE: CookingDatabase? = null

        private const val TAG = "CookingDatabase"
        private const val ASSET_DB_NAME = "products.db"
        private const val DEVICE_DB_NAME = "cooking_database.db"
        var currentUserId: Long = 1

        fun getDatabase(context: Context): CookingDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Создание базы данных")

                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        CookingDatabase::class.java,
                        DEVICE_DB_NAME
                    )
                        .createFromAsset("databases/$ASSET_DB_NAME")
                        .fallbackToDestructiveMigration()
                        .build()

                    Log.d(TAG, "База загружена из assets/databases/$ASSET_DB_NAME")
                    INSTANCE = instance

                    /*CoroutineScope(Dispatchers.IO).launch {
                    }*/

                    instance

                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка загрузки базы: ${e.message}")
                    throw e
                }
            }
        }

    }
}