// domain/models/ShownRecipes.kt
package com.example.appforcooking.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShownRecipes(
    var showEasy: Boolean = true,
    var showMid: Boolean = true,
    var showHard: Boolean = true,
    var hideAllergyRecipes: Boolean = true // true - показывать рецепты с аллергенами, false - скрывать
) : Parcelable {

    fun isAnySelected(): Boolean {
        return showEasy || showMid || showHard
    }

    fun getSelectedDifficulties(): List<String> {
        val selected = mutableListOf<String>()
        if (showEasy) selected.add("Легко")
        if (showMid) selected.add("Средне")
        if (showHard) selected.add("Сложно")
        return selected
    }

    companion object {
        fun all() = ShownRecipes(true, true, true, true)
        fun none() = ShownRecipes(false, false, false, false)
    }
}