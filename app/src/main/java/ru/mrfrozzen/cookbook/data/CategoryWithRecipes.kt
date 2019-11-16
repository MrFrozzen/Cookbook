package ru.mrfrozzen.cookbook.data

import androidx.room.Embedded
import androidx.room.Relation
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

class CategoryWithRecipes {

    @Embedded
    var category: Category? = null

    @Relation(parentColumn = "category_id", entityColumn = "category_category_id", entity = Recipe::class)
    var recipes: List<Recipe>? = null
}
