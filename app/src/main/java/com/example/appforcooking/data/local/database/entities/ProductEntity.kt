package com.example.appforcooking.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "product_id")
    val productId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "default_unit")
    val defaultUnit: String,

    @ColumnInfo(name = "calories_per_100g")
    val caloriesPer100g: Int,

    @ColumnInfo(name = "barcode")
    val barcode: String = ""
)