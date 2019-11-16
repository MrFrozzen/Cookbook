package ru.mrfrozzen.cookbook.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.SET_NULL
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = arrayOf("category_id"),
        childColumns = arrayOf("category_category_id"),
        onUpdate = CASCADE,
        onDelete = SET_NULL)])
data class Recipe(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "recipe_id") val id: Int,
        @ColumnInfo(name = "recipe_name") var name: String,
        @ColumnInfo(name = "category_category_id") var categoryId: Int?,
        @ColumnInfo(name = "ingredients") var ingredients: List<String>,
        @ColumnInfo(name = "instructions") var instructions: String,
        @ColumnInfo(name = "favorite") var favorite: Int = 0,
        @ColumnInfo(name = "image_path") var imagePath: String?
) {
    override fun toString() = name
}
