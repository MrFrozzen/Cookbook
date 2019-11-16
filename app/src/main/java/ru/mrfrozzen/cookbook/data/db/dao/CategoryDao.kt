package ru.mrfrozzen.cookbook.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.mrfrozzen.cookbook.data.CategoryWithRecipes
import ru.mrfrozzen.cookbook.data.db.entity.Category

@Dao
interface CategoryDao {
    @Transaction
    @Query("SELECT * FROM Category ORDER BY Category.category_name ASC")
    fun getCategoriesWithRecipes(): LiveData<List<CategoryWithRecipes>>

    @Query("SELECT * FROM Category ORDER BY category_name ASC")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM Category WHERE category_id = :id ORDER BY category_name ASC")
    fun getCategory(id: Int?): LiveData<Category>

    @Delete
    fun delete(category: Category)
    
    @Query("DELETE FROM Category WHERE category_id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM Category")
    fun deleteAll()

    @Update
    fun update(category: Category)

    @Insert
    fun insert(category: Category)

    @Insert
    fun insertAll(categoryEntities: List<Category>)
}
