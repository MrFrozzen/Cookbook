package ru.mrfrozzen.cookbook.ui


import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.presentation.main.MainActivity
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers.anything
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class EditRecipeTest {

    @Rule @JvmField val activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before fun populateDb() {
        DatabaseHelper.repopulateDb()
        onView(isRoot()).perform(MainActivityTest.waitFor(1000))
    }

    @Test fun editRecipeTest_fromViewRecipe() {
        val editedRecipeIndex = 3
        val name = "ZThis name is edited"
        val instructions = "These are the edited instructions."
        val ingredient1 = "Ingredient 1"
        val category = "Starter"
        val noCategory = "No category"

        // View the recipe 'Test recipe'
        onData(anything()).inAdapterView(withId(R.id.list_recipes_all)).atPosition(editedRecipeIndex)
                .perform(click())

        // Edit the recipe, change category
        onView(withId(R.id.action_edit_recipe))
                .perform(click())

        onView(withId(R.id.edit_instructions))
                .perform(replaceText(instructions), closeSoftKeyboard())

        onView(withId(R.id.button_remove_ingredient))
                .perform(click())

        onView(withId(R.id.edit_name))
                .perform(replaceText(name))

        onView(withId(R.id.spinner_category))
                .perform(click())

        onView(withText(category))
                .perform(click())

        onView(withId(R.id.action_save))
                .perform(click())

        // Ensure views changed in ViewRecipeActivity
        onView(withId(R.id.text_view_category))
                .check(matches(isDisplayed()))
                .check(matches(withText(category)))

        onView(withId(R.id.text_view_ingredients))
                .check(matches(withText("$ingredient1\n")))

        onView(withId(R.id.text_view_instructions))
                .check(matches(withText(instructions)))

        // Edit the recipe again, remove category
        onView(withId(R.id.action_edit_recipe))
                .perform(click())

        onView(withId(R.id.checkbox_category))
                .perform(click())

        onView(withId(R.id.action_save))
                .perform(click())

        // Ensure category no longer shown in ViewRecipeActivity
        onView(withId(R.id.text_view_category))
                .check(matches(not(isDisplayed())))

        // Check that recipe updated in MainActivity
        Espresso.pressBack()
        onData(anything()).inAdapterView(withId(R.id.list_recipes_all)).atPosition(editedRecipeIndex)
                .check(matches(hasDescendant(withText(name))))
                .check(matches(hasDescendant(withText(noCategory))))
    }

    @Test fun editRecipeTest_fromCategoryList() {
        val categoryTab = "CATEGORIES"
        val editedCategory = "Dessert"
        val name = "ZThis name is edited"
        val contextEdit = "Edit"

        onView(withText(categoryTab))
                .perform(click())

        // Expand category with the edited recipe
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(1)
                .perform(click())

        // Edit 'Test recipe' through context menu
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(3)
                .perform(longClick())
        onView(withText(contextEdit))
                .perform(click())

        // Now edit the recipe, but don't change the category
        onView(withId(R.id.edit_name))
                .perform(replaceText(name))

        onView(withId(R.id.action_save))
                .perform(click())

        // Ensure recipe updated in Category list
        // Expand category 'Desserts'
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(1)
                .perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(3)
                .check(matches(hasDescendant(withText(name))))

        // Now edit the recipe again, but change the category
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(3)
                .perform(longClick())
        onView(withText(contextEdit))
                .perform(click())
        onView(withId(R.id.spinner_category))
                .perform(click())
        onView(withText(editedCategory))
                .perform(click())
        onView(withId(R.id.action_save))
                .perform(click())

        // Ensure recipe moved under the right category in category list
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(0)
                .perform(click())
        onData(anything()).inAdapterView(withId(R.id.list_categories)).atPosition(2)
                .check(matches(hasDescendant(withText(name))))
    }
}
