package ru.mrfrozzen.cookbook.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.mrfrozzen.cookbook.data.db.AppDatabase
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

class DatabaseTestDummyRecipeWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val TAG by lazy { DatabaseTestDummyRecipeWorker::class.java.simpleName }

    override fun doWork(): Result {
        return try {
            val recipe = Recipe(4, "Test recipe", 2, arrayListOf("Ingredient 1", "Ingredient 2"), "These are the instructions.", 0, null)
            val database = AppDatabase.getInstance(applicationContext)
            database.recipeDao().insert(recipe)
            Result.success()
        } catch (ex: Exception) {
            //Log.e(TAG, "Error seeding test recipes to database", ex)
            Result.failure()
        }
    }
}