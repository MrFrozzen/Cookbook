package ru.mrfrozzen.cookbook.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.mrfrozzen.cookbook.data.db.converter.ListConverter
import ru.mrfrozzen.cookbook.data.db.dao.CategoryDao
import ru.mrfrozzen.cookbook.data.db.dao.RecipeDao
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.utilities.DB_NAME

@Database(entities = [Recipe::class, Category::class], version = 2)
@TypeConverters(ListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                        "CREATE TABLE `Temp_category` ("
                                + "`category_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                                + "`category_name` TEXT NOT NULL)"
                )
                database.execSQL("INSERT INTO Temp_category SELECT * FROM Category")
                database.execSQL("DROP TABLE Category")
                database.execSQL("ALTER TABLE Temp_category RENAME TO Category")

                database.execSQL(
                        "CREATE TABLE `Temp_recipe` ("
                                + "`recipe_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                                + "`recipe_name` TEXT NOT NULL, "
                                + "`category_category_id` INTEGER DEFAULT NULL REFERENCES Category(category_id) "
                                + "ON UPDATE CASCADE ON DELETE SET NULL, "
                                + "`ingredients` TEXT NOT NULL, "
                                + "`instructions` TEXT NOT NULL, "
                                + "`favorite` INTEGER NOT NULL, "
                                + "`image_path` TEXT DEFAULT NULL)"
                )
                database.execSQL("INSERT INTO Temp_recipe (recipe_id, recipe_name, category_category_id, " + "ingredients, instructions, favorite) SELECT * FROM Recipe")
                database.execSQL("DROP TABLE Recipe")
                database.execSQL("ALTER TABLE Temp_recipe RENAME TO Recipe")
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                    .addMigrations(MIGRATION_1_2)
                    .build()
        }
    }
}
