package ru.mrfrozzen.cookbook.presentation.view

import androidx.lifecycle.ViewModel
import ru.mrfrozzen.cookbook.data.DataRepository
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

class ViewRecipeViewModel internal constructor(private val dataRepository: DataRepository) : ViewModel() {

    fun getRecipeWithCategory(id: Int) = dataRepository.getRecipeWithCategory(id)

    fun deleteRecipe(recipe: Recipe) {
        dataRepository.deleteRecipe(recipe)
    }

    fun updateRecipe(recipe: Recipe) {
        dataRepository.updateRecipe(recipe)
    }
}
