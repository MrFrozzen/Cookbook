package ru.mrfrozzen.cookbook.presentation.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.mrfrozzen.cookbook.data.DataRepository

class ViewRecipeViewModelFactory(private val repository: DataRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ViewRecipeViewModel(repository) as T
    }
}