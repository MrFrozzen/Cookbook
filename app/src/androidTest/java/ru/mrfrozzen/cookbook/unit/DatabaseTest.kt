package ru.mrfrozzen.cookbook.unit

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import ru.mrfrozzen.cookbook.data.db.AppDatabase
import ru.mrfrozzen.cookbook.data.db.dao.CategoryDao
import ru.mrfrozzen.cookbook.data.db.dao.RecipeDao
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsEmptyCollection.empty
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SmallTest
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var appDatabase: AppDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var categoryDao: CategoryDao

    @get:Rule var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Throws(InterruptedException::class)
    fun <T> LiveData<T>.getValueBlocking(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)
        val innerObserver = Observer<T> {
            value = it
            latch.countDown()
        }
        observeForever(innerObserver)
        latch.await(2, TimeUnit.SECONDS)
        return value
    }

    @Before fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        recipeDao = appDatabase.recipeDao()
        categoryDao = appDatabase.categoryDao()

        val ingredients = ArrayList<String>()
        ingredients.add("Test ingredient 1")
        ingredients.add("Test ingredient 2")
        val recipe = Recipe(0, TEST_RECIPE_NAME, 69, ingredients, "Test instructions", 0, "file://test/image.jpg")

        val category = Category(69, TEST_CATEGORY_NAME)

        categoryDao.insert(category)
        recipeDao.insert(recipe)
    }

    @After fun closeDb() {
        appDatabase.close()
    }

    @Test fun readCategoriesWithRecipes() {
        val categoriesWithRecipes = categoryDao.getCategoriesWithRecipes().getValueBlocking()

        assertThat(categoriesWithRecipes!!.size, `is`(1))
        assertThat(categoriesWithRecipes[0].recipes!!.size, `is`(1))
        assertThat<String>(categoriesWithRecipes[0].category!!.name, `is`(TEST_CATEGORY_NAME))
        assertThat<String>(categoriesWithRecipes[0].recipes!![0].name, `is`(TEST_RECIPE_NAME))

    }

    @Test fun readRecipesWithCategory() {
        val recipesWithCategory = recipeDao.getRecipesWithCategory().getValueBlocking()

        assertThat(recipesWithCategory!!.size, `is`(1))
        assertThat<String>(recipesWithCategory[0].category!!.name, `is`(TEST_CATEGORY_NAME))
        assertThat<String>(recipesWithCategory[0].recipe.name, `is`(TEST_RECIPE_NAME))

    }

    @Test fun deleteCategory() {
        val category = categoryDao.getCategoriesWithRecipes().getValueBlocking()!![0].category
        categoryDao.delete(category!!)

        val recipe = recipeDao.getAll().getValueBlocking()!![0]
        assertThat<Int>(recipe.categoryId, nullValue())

        val recipesWithCategories = recipeDao.getRecipesWithCategory().getValueBlocking()
        assertThat(recipesWithCategories!![0].category, nullValue())

        val categoriesWithRecipes = categoryDao.getCategoriesWithRecipes().getValueBlocking()
        assertThat(categoriesWithRecipes, empty())

    }

    @Test fun updateCategory() {
        var category = recipeDao.getRecipesWithCategory().getValueBlocking()!![0].category
        category!!.name = "new"
        categoryDao.update(category)

        category = categoryDao.getAll().getValueBlocking()!![0]
        assertThat<String>(category.name, `is`("new"))

        val recipesWithCategories = recipeDao.getRecipesWithCategory().getValueBlocking()!!
        assertThat<String>(recipesWithCategories[0].category!!.name, `is`("new"))

        val categoriesWithRecipes = categoryDao.getCategoriesWithRecipes().getValueBlocking()!!
        assertThat<String>(categoriesWithRecipes[0].category!!.name, `is`("new"))

    }

    companion object {
        private const val TEST_RECIPE_NAME = "Test recipe"
        private const val TEST_CATEGORY_NAME = "Test category"
    }
}
