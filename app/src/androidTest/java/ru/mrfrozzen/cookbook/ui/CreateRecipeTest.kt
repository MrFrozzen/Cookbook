package ru.mrfrozzen.cookbook.ui


import android.view.View
import android.view.ViewGroup
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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CreateRecipeTest {

    @Rule @JvmField var activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun populateDb() {
        DatabaseHelper.repopulateDb()
        onView(isRoot()).perform(MainActivityTest.waitFor(1000))
    }

    @Test
    fun createRecipeTest_noCategory() {
        val name = "A new recipe"
        val ingredient1 = "This is ingredient number 1"
        val ingredient2 = "This is ingredient number 2"
        val instructions = "These are the instructions."
        val noCategory = "No category"

        Thread.sleep(700)

        onView(withId(R.id.button_add))
                .perform(click())

        onView(withId(R.id.button_add_recipe))
                .perform(click())

        Thread.sleep(700)

        onView(withId(R.id.edit_instructions))
                .perform(replaceText(instructions), closeSoftKeyboard())

        onView(allOf(childAtPosition(childAtPosition(withId(R.id.table_ingredients), 0), 0), isDisplayed()))
                        .perform(replaceText(ingredient1), closeSoftKeyboard())

        onView(withId(R.id.button_add_ingredient))
                .perform(click())
                .perform(click())

        onView(allOf(childAtPosition(childAtPosition(withId(R.id.table_ingredients), 2), 0), isDisplayed()))
                .perform(replaceText(ingredient2), closeSoftKeyboard())

        onView(withId(R.id.edit_name))
                .perform(replaceText(name), closeSoftKeyboard())

        onView(withId(R.id.action_save))
                .perform(click())

        Thread.sleep(700)

        onData(anything()).inAdapterView(withId(R.id.list_recipes_all)).atPosition(0)
                .check(matches(hasDescendant(withText(name))))
                .check(matches(hasDescendant(withText(noCategory))))
                .perform(click())

        Thread.sleep(700)

        onView(withId(R.id.text_view_category))
                .check(matches(not(isDisplayed())))

        onView(withId(R.id.text_view_ingredients))
                .check(matches(withText("$ingredient1\n$ingredient2\n")))

        onView(withId(R.id.text_view_instructions))
                .check(matches(withText(instructions)))

        // TODO: test recipe name
    }

    @Test
    fun createRecipeTest_withCategory() {
        val name = "A new recipe"
        val ingredient1 = "This is ingredient number 1"
        val ingredient2 = "This is ingredient number 2"
        val instructions = "These are the instructions."
        val category = "Dessert"

        Thread.sleep(700)

        onView(withId(R.id.button_add))
                .perform(click())

        onView(withId(R.id.button_add_recipe))
                .perform(click())

        Thread.sleep(700)

        onView(withId(R.id.edit_instructions))
                .perform(replaceText(instructions), closeSoftKeyboard())

        onView(allOf(childAtPosition(childAtPosition(withId(R.id.table_ingredients), 0), 0), isDisplayed()))
                .perform(replaceText(ingredient1), closeSoftKeyboard())

        onView(withId(R.id.button_add_ingredient))
                .perform(click())
                .perform(click())

        onView(allOf(childAtPosition(childAtPosition(withId(R.id.table_ingredients), 2), 0), isDisplayed()))
                .perform(replaceText(ingredient2), closeSoftKeyboard())

        onView(withId(R.id.edit_name))
                .perform(replaceText(name), closeSoftKeyboard())

        onView(withId(R.id.checkbox_category))
                .perform(click())

        onView(withId(R.id.spinner_category))
                .perform(click())

        onView(withText(category))
                .perform(click())

        onView(withId(R.id.action_save))
                .perform(click())

        Thread.sleep(700)

        onData(anything()).inAdapterView(withId(R.id.list_recipes_all)).atPosition(0)
                .check(matches(hasDescendant(withText(name))))
                .check(matches(hasDescendant(withText(category))))
                .perform(click())

        Thread.sleep(700)

        onView(withId(R.id.text_view_category))
                .check(matches(isDisplayed()))
                .check(matches(withText(category)))

        onView(withId(R.id.text_view_ingredients))
                .check(matches(withText("$ingredient1\n$ingredient2\n")))

        onView(withId(R.id.text_view_instructions))
                .check(matches(withText(instructions)))

        // TODO: test recipe name
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
