package ru.mrfrozzen.cookbook.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.mrfrozzen.cookbook.data.DataRepository

class EditrecipeViewModelFactory(private val repository: DataRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        return EditRecipeViewModel(repository) as T
    }
}