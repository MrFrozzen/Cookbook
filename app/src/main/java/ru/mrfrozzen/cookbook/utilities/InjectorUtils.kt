package ru.mrfrozzen.cookbook.utilities

import android.content.Context
import ru.mrfrozzen.cookbook.data.DataRepository
import ru.mrfrozzen.cookbook.data.db.AppDatabase
import ru.mrfrozzen.cookbook.presentation.edit.EditrecipeViewModelFactory
import ru.mrfrozzen.cookbook.presentation.main.MainViewModelFactory
import ru.mrfrozzen.cookbook.presentation.view.ViewRecipeViewModelFactory

object InjectorUtils {

    private fun getDataRepository(context: Context): DataRepository {
        val db = AppDatabase.getInstance(context)
        return DataRepository.getInstance(db.recipeDao(), db.categoryDao())
    }

    fun provideEditRecipeViewModelFactory(context: Context): EditrecipeViewModelFactory {
        val repository = getDataRepository(context)
        return EditrecipeViewModelFactory(repository)
    }

    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        val repository = getDataRepository(context)
        return MainViewModelFactory(repository)
    }

    fun provideViewRecipeViewModelFactory(context: Context): ViewRecipeViewModelFactory {
        val repository = getDataRepository(context)
        return ViewRecipeViewModelFactory(repository)
    }
}