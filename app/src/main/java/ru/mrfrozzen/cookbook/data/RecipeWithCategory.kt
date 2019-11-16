package ru.mrfrozzen.cookbook.data

import androidx.room.Embedded
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

data class RecipeWithCategory(@Embedded val recipe: Recipe, @Embedded val category: Category?) {

    override fun toString() = recipe.name
}
