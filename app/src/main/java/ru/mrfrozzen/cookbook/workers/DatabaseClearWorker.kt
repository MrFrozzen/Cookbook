package ru.mrfrozzen.cookbook.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.mrfrozzen.cookbook.data.db.AppDatabase

class DatabaseClearWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private val TAG by lazy { DatabaseClearWorker::class.java.simpleName }

    override fun doWork(): Result {

        return try {
            val database = AppDatabase.getInstance(applicationContext)
            database.categoryDao().deleteAll()
            database.recipeDao().deleteAll()
            Result.success()
        } catch (ex: Exception) {
            //Log.e(TAG, "Error clearing database", ex)
            Result.failure()
        }
    }
}