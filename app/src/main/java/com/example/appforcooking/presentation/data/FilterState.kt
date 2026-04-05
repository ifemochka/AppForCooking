package com.example.appforcooking.presentation.data

import com.example.appforcooking.domain.models.ShownRecipes

object FilterState {
    var currentFilters: ShownRecipes = ShownRecipes.all()
}