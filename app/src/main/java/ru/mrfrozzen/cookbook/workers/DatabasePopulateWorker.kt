package ru.mrfrozzen.cookbook.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.mrfrozzen.cookbook.data.db.AppDatabase
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.utilities.CATEGORY_DATA_FILENAME
import ru.mrfrozzen.cookbook.utilities.RECIPE_DATA_FILENAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader

class DatabasePopulateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val TAG by lazy { DatabasePopulateWorker::class.java.simpleName }

    override fun doWork(): Result {
        val categoryType = object : TypeToken<List<Category>>() {}.type
        val recipeType = object : TypeToken<List<Recipe>>() {}.type
        var jsonReader: JsonReader? = null

        return try {
            var inputStream = applicationContext.assets.open(CATEGORY_DATA_FILENAME)
            jsonReader = JsonReader(inputStream.reader())
            val categoryList: List<Category> = Gson().fromJson(jsonReader, categoryType)

            inputStream = applicationContext.assets.open(RECIPE_DATA_FILENAME)
            jsonReader = JsonReader(inputStream.reader())
            val recipeList: List<Recipe> = Gson().fromJson(jsonReader, recipeType)
            val database = AppDatabase.getInstance(applicationContext)
            database.categoryDao().insertAll(categoryList)
            database.recipeDao().insertAll(recipeList)
            Result.success()
        } catch (ex: Exception) {
            //Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        } finally {
            jsonReader?.close()
        }
    }
}