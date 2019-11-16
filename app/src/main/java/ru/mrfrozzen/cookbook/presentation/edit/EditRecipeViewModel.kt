package ru.mrfrozzen.cookbook.presentation.edit

import android.widget.AdapterView.INVALID_POSITION
import androidx.lifecycle.ViewModel
import ru.mrfrozzen.cookbook.data.DataRepository
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

class EditRecipeViewModel internal constructor(private val dataRepository: DataRepository): ViewModel() {

    var mode: Int = 0
    var selectedCategorySpinnerIndex = INVALID_POSITION
    var isCategoryEnabled = false
    var editedRecipe: Recipe? = null
    var editedRecipeImagePath: String? = null
    var editedRecipeCategory: Category? = null
    var currentVisibleImageFile: String? = null
    var imagePlaceholderFile: String? = null
    var imageCleared = false
    val categories = dataRepository.getCategories()

    fun getRecipeWithCategory(id: Int) = dataRepository.getRecipeWithCategory(id)

    fun insertRecipe(recipe: Recipe) {
        dataRepository.insertRecipe(recipe)
    }

    fun updateRecipe(recipe: Recipe) {
        dataRepository.updateRecipe(recipe)
    }
}
