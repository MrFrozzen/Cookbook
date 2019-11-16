package ru.mrfrozzen.cookbook.data

import ru.mrfrozzen.cookbook.data.db.dao.CategoryDao
import ru.mrfrozzen.cookbook.data.db.dao.RecipeDao
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.utilities.runOnIoThread
import java.io.File

class DataRepository private constructor(
        private val recipeDao: RecipeDao,
        private val categoryDao: CategoryDao
) {
    fun getCategories() = categoryDao.getAll()

    fun getCategoriesWithRecipes() = categoryDao.getCategoriesWithRecipes()

    fun getFavoriteRecipesWithCategory() = recipeDao.getFavoriteRecipesWithCategory()

    fun getRecipeWithCategory(id: Int) = recipeDao.getRecipeWithCategory(id)

    fun getRecipesWithCategory() = recipeDao.getRecipesWithCategory()

    fun deleteAllCategories() {
        runOnIoThread { categoryDao.deleteAll() }
    }

    fun deleteAllRecipes() {
        runOnIoThread { recipeDao.deleteAll() }
    }

    fun deleteCategory(category: Category) {
        runOnIoThread { categoryDao.delete(category) }
    }

    fun deleteRecipe(recipe: Recipe) {
        runOnIoThread {
            val imagePath = recipe.imagePath
            if (imagePath != null) {
                val file = File(imagePath)
                val deleted = file.delete()
            }
            recipeDao.delete(recipe)
        }
    }

    fun insertCategory(category: Category) {
        runOnIoThread { categoryDao.insert(category) }
    }

    fun insertRecipe(recipe: Recipe) {
        runOnIoThread { recipeDao.insert(recipe) }
    }

    fun updateCategory(category: Category) {
        runOnIoThread { categoryDao.update(category) }
    }

    fun updateRecipe(recipe: Recipe) {
        runOnIoThread { recipeDao.update(recipe) }
    }

    companion object {
        private val TAG by lazy { DataRepository::class.java.simpleName }
        @Volatile private var instance: DataRepository? = null

        fun getInstance(recipeDao: RecipeDao, categoryDao: CategoryDao) =
                instance ?: synchronized(this) {
                    instance ?: DataRepository(recipeDao, categoryDao).also { instance = it }
                }
    }
}
