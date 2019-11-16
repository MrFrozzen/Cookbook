package ru.mrfrozzen.cookbook.ui

import android.view.View
import android.widget.AutoCompleteTextView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.presentation.main.MainActivity
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@LargeTest
class MainActivityTest {

    @get:Rule val activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    companion object {
        const val RECIPES_TAB = "RECIPES"
        const val FAVORITES_TAB = "FAVORITES"
        const val CATEGORIES_TAB = "CATEGORIES"
        const val CONTEXT_EDIT = "Edit"
        const val CONTEXT_DELETE = "Delete"
        const val ACTION_SAVE = "SAVE"
        const val ACTION_DELETE = "DELETE"
        const val NO_CATEGORY = "No category"
        const val BROWNIES = "Best brownies"
        const val BREAD = "Simple whole wheat bread"
        const val MAC = "Macaroni and Cheese"
        const val DESSERT = "Dessert"
        const val MAIN_COURSE = "Main course"

        fun waitFor(millis: Long) : ViewAction {
            return object : ViewAction {
                override fun getConstraints() : Matcher<View> { return isRoot() }
                override fun getDescription(): String { return "Wait for $millis milliseconds" }
                override fun perform(uiController: UiController?, view: View?) { uiController!!.loopMainThreadForAtLeast(millis) }
            }
        }
    }

    @Before fun populateDb() {
        DatabaseHelper.repopulateDb()
        onView(isRoot()).perform(waitFor(1000))
    }

    @Test fun allRecipes_deleteRecipe() {
        val name = BROWNIES

        deleteRecipe(0)

        // Recipe no longer in list
        onView(withId(R.id.list_recipes_all))
                .check(matches(not(hasDescendant(withText(name)))))

        // No favorites text should be visible
        onView(withText(FAVORITES_TAB))
                .perform(click())

        onView(withId(R.id.text_no_favorites))
                .check(matches(isDisplayed()))
    }

    @Test fun allRecipes_noRecipesText() {
        // No recipes text is not visible
        onView(withId(R.id.text_no_recipes))
                .check(matches(not(isDisplayed())))

        for (i in 3 downTo 0) deleteRecipe(i)

        // No recipes text is visible
        onView(withId(R.id.text_no_recipes))
                .check(matches(isDisplayed()))
    }

    @Test fun editCategory() {
        val originalName = DESSERT
        val name = "AA New category"

        // Edit category
        onView(withText(CATEGORIES_TAB))
                .perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(0)
                .perform(longClick())
        onView(withText(CONTEXT_EDIT))
                .perform(click())
        onView(withText(originalName))
                .perform(replaceText(name))
        onView(withText(ACTION_SAVE))
                .perform(click())

        // Original category name no longer in list
        onView(withId(R.id.list_categories))
                .check(matches(not(hasDescendant(withText(originalName)))))

        // New category name now in list
        onView(withId(R.id.list_categories))
                .check(matches(hasDescendant(withText(name))))

        // Category should change in favorites list and all recipes list
        onView(withText(FAVORITES_TAB))
                .perform(click())
        onView(withId(R.id.list_favorites))
                .check(matches(hasDescendant(withText(name))))
        onView(withId(R.id.list_favorites))
                .check(matches(hasDescendant(not(withText(originalName)))))

        onView(withText(RECIPES_TAB))
                .perform(click())
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(withText(name))))
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(not(withText(originalName)))))
    }

    @Test fun deleteCategory() {
        val deleted = MAIN_COURSE

        // Delete category
        onView(withText(CATEGORIES_TAB))
                .perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(1)
                .perform(longClick())
        onView(withText(CONTEXT_DELETE))
                .perform(click())
        onView(withText(ACTION_DELETE))
                .perform(click())

        // Category no longer in list
        onView(withId(R.id.list_categories))
                .check(matches(not(hasDescendant(withText(deleted)))))

        // Category should change in recipes list
        onView(withText(RECIPES_TAB))
                .perform(click())
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(withText(NO_CATEGORY))))
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(not(withText(deleted)))))
    }

    @Test fun searchRecipe() {
        val searchPart = "brown"

        // Search for a recipe
        onView(withId(R.id.action_search))
                .perform(click())
        onView(isAssignableFrom(AutoCompleteTextView::class.java))
                .perform(typeText(searchPart))

        // Brownies should be in list
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(withText(BROWNIES))))

        // Bread should not be in list
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(not(withText(BREAD)))))

        // Search for main courses
        onView(isAssignableFrom(AutoCompleteTextView::class.java))
                .perform(clearText())
                .perform(typeText(MAIN_COURSE))

        // Mac should be in list
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(withText(MAC))))

        // Brownies should not be in list
        onView(withId(R.id.list_recipes_all))
                .check(matches(hasDescendant(not(withText(BROWNIES)))))
    }

    @Test fun categories_noCategoriesText() {
        // No categories text should not be visible
        onView(withId(R.id.text_no_recipes))
                .check(matches(not(isDisplayed())))

        // Delete all 9 categories
        onView(withText(CATEGORIES_TAB))
                .perform(click())

        for (i in 2 downTo 0) {
            onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(i)
                    .perform(longClick())
            onView(withText(CONTEXT_DELETE))
                    .perform(click())
            onView(withText(ACTION_DELETE))
                    .perform(click())
        }

        // No categories text should be visible
        onView(withId(R.id.text_no_categories))
                .check(matches(isDisplayed()))
    }

    private fun deleteRecipe(index: Int) {
        onData(anything()).inAdapterView(withId(R.id.list_recipes_all)).atPosition(index)
                .perform(longClick())

        onView(withText("Delete"))
                .perform(click())

        onView(withText("DELETE"))
                .perform(click())
    }
}