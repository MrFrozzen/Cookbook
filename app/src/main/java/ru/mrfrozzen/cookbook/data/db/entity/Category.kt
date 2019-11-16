package ru.mrfrozzen.cookbook.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "category_id") val id: Int,
        @ColumnInfo(name = "category_name") var name: String
) {
    override fun toString()  = name
}
