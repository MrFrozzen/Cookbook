package ru.mrfrozzen.cookbook.ui

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ru.mrfrozzen.cookbook.workers.DatabaseClearWorker
import ru.mrfrozzen.cookbook.workers.DatabasePopulateWorker
import ru.mrfrozzen.cookbook.workers.DatabaseTestDummyRecipeWorker

class DatabaseHelper {

    companion object {
        fun repopulateDb() {
            val workManager = WorkManager.getInstance()
            val clear = OneTimeWorkRequestBuilder<DatabaseClearWorker>().build()
            val populate = OneTimeWorkRequestBuilder<DatabasePopulateWorker>().build()
            val testRecipe = OneTimeWorkRequestBuilder<DatabaseTestDummyRecipeWorker>().build()
            workManager.beginWith(clear).then(populate).then(testRecipe).enqueue()
        }
    }
}