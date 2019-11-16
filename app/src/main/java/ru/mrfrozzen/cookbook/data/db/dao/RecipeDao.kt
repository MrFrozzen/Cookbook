package ru.mrfrozzen.cookbook.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

@Dao
interface RecipeDao {
    @Query("SELECT * FROM Recipe LEFT OUTER JOIN Category ON Recipe.category_category_id = Category.category_id WHERE Recipe.recipe_id = :id ORDER BY Recipe.recipe_name ASC")
    fun getRecipeWithCategory(id: Int): LiveData<RecipeWithCategory>

    @Query("SELECT * FROM Recipe LEFT OUTER JOIN Category ON Recipe.category_category_id = Category.category_id ORDER BY Recipe.recipe_name ASC")
    fun getRecipesWithCategory(): LiveData<List<RecipeWithCategory>>

    @Query("SELECT * FROM Recipe LEFT OUTER JOIN Category ON Recipe.category_category_id = Category.category_id WHERE Recipe.favorite = 1 ORDER BY Recipe.recipe_name ASC")
    fun getFavoriteRecipesWithCategory(): LiveData<List<RecipeWithCategory>>

    @Query("SELECT * FROM Recipe ORDER BY recipe_name ASC")
    fun getAll(): LiveData<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE favorite = 1 ORDER BY recipe_name ASC")
    fun getFavorites(): LiveData<List<Recipe>>

    @Insert
    fun insert(recipe: Recipe)

    @Insert
    fun insertAll(recipeEntities: List<Recipe>)

    @Update
    fun update(recipe: Recipe)

    @Delete
    fun delete(recipe: Recipe)

    @Query("DELETE FROM Recipe")
    fun deleteAll()
}
