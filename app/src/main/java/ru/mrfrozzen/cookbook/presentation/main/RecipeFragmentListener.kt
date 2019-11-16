package ru.mrfrozzen.cookbook.presentation.main

import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

internal interface RecipeFragmentListener {
    fun onCategoryEdit(category: Category)

    fun onCategoryDelete(category: Category)

    fun onRecipeDelete(recipe: Recipe)

    fun onRecipeEdit(id: Int)

    fun onRecipeSelected(id: Int)

    fun onRecipeShare(rwc: RecipeWithCategory)
}
