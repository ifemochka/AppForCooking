package com.example.appforcooking.data.local.mappers

import com.example.appforcooking.data.local.database.entities.RecipeEntity
import com.example.appforcooking.domain.models.Recipe

object RecipeMapper {
    fun toDomain(entity: RecipeEntity): Recipe {
        return Recipe(
            recipeId = entity.recipeId,
            title = entity.title,
            description = entity.description,
            cookingTimeMinutes = entity.cookingTimeMinutes,
            difficulty = entity.difficulty,
            imageUrl = entity.imageUrl,
            caloriesTotal = entity.caloriesTotal,
            instructions = entity.instructions
        )
    }

    fun toDomain(entities: List<RecipeEntity>): List<Recipe> {
        return entities.map { toDomain(it) }
    }
}