package ru.mrfrozzen.cookbook.unit

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import ru.mrfrozzen.cookbook.data.db.AppDatabase
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasItemInArray
import org.hamcrest.core.IsNull.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@MediumTest
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest internal constructor() {

    @get:Rule var helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            Objects.requireNonNull(AppDatabase::class.java.canonicalName),
            FrameworkSQLiteOpenHelperFactory())

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1)

        db.execSQL("INSERT INTO Category VALUES (0, 'Test category')")
        db.execSQL("INSERT INTO Recipe VALUES (0, 'Test recipe 1', 0, 'Test ingredients', " + "'Test instructions', 0)")
        db.execSQL("INSERT INTO Recipe VALUES (1, 'Test recipe 2', NULL, 'Test ingredients', " + "'Test instructions', 1)")

        db.close()

        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        // RECIPE TABLE VALIDATION
        var cursor = db.query("SELECT * FROM Recipe")

        // Check that there are 2 recipes, and 7 columns
        assertThat(cursor.columnCount, `is`(7))
        assertThat(cursor.count, `is`(2))

        // Check changed column names
        var columnNames = cursor.columnNames
        assertThat(columnNames, hasItemInArray("recipe_id"))
        assertThat(columnNames, hasItemInArray("recipe_name"))
        assertThat(columnNames, hasItemInArray("category_category_id"))
        assertThat(columnNames, hasItemInArray("ingredients"))
        assertThat(columnNames, hasItemInArray("instructions"))
        assertThat(columnNames, hasItemInArray("favorite"))
        assertThat(columnNames, hasItemInArray("image_path"))

        // Use getString() with index 2 (categoryId), to catch nulls
        // Test recipe 1 data
        cursor.moveToNext()
        assertThat(cursor.getInt(0), `is`(0))
        assertThat(cursor.getString(1), `is`("Test recipe 1"))
        assertThat(cursor.getString(2), `is`("0"))
        assertThat(cursor.getString(3), `is`("Test ingredients"))
        assertThat(cursor.getString(4), `is`("Test instructions"))
        assertThat(cursor.getInt(5), `is`(0))
        assertThat(cursor.getString(6), `is`(nullValue()))

        // Test recipe 2 data
        cursor.moveToNext()
        assertThat(cursor.getInt(0), `is`(1))
        assertThat(cursor.getString(1), `is`("Test recipe 2"))
        assertThat(cursor.getString(2), nullValue())
        assertThat(cursor.getString(3), `is`("Test ingredients"))
        assertThat(cursor.getString(4), `is`("Test instructions"))
        assertThat(cursor.getInt(5), `is`(1))
        assertThat(cursor.getString(6), `is`(nullValue()))


        // CATEGORY TABLE VALIDATION
        cursor = db.query("SELECT * FROM Category")

        // Check that there is 1 category, and 2 columns
        assertThat(cursor.columnCount, `is`(2))
        assertThat(cursor.count, `is`(1))

        // Check changed column names
        columnNames = cursor.columnNames
        assertThat(columnNames, hasItemInArray("category_id"))
        assertThat(columnNames, hasItemInArray("category_name"))

        // Test category data
        cursor.moveToNext()
        assertThat(cursor.getInt(0), `is`(0))
        assertThat(cursor.getString(1), `is`("Test category"))

        db.close()
    }

    companion object {
        private const val TEST_DB = "cookbook_db"
    }
}
