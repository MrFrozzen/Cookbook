package ru.mrfrozzen.cookbook.presentation.main

import androidx.lifecycle.ViewModel
import ru.mrfrozzen.cookbook.data.DataRepository
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

class MainViewModel internal constructor(private val dataRepository: DataRepository) : ViewModel() {

    val categoriesWithRecipes = dataRepository.getCategoriesWithRecipes()
    val recipesWithCategory = dataRepository.getRecipesWithCategory()
    val favoriteRecipesWithCategory = dataRepository.getFavoriteRecipesWithCategory()

    fun deleteAllCategories() {
        dataRepository.deleteAllCategories()
    }

    fun deleteAllRecipes() {
        dataRepository.deleteAllRecipes()
    }

    fun deleteCategory(category: Category) {
        dataRepository.deleteCategory(category)
    }

    fun deleteRecipe(recipe: Recipe) {
        dataRepository.deleteRecipe(recipe)
    }

    fun insertCategory(category: Category) {
        dataRepository.insertCategory(category)
    }

    fun updateCategory(category: Category) {
        dataRepository.updateCategory(category)
    }

    fun updateRecipe(recipe: Recipe) {
        dataRepository.updateRecipe(recipe)
    }
}
