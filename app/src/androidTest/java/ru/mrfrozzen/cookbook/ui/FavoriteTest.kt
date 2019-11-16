package ru.mrfrozzen.cookbook.ui


import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
class FavoriteTest {

    @Rule @JvmField val activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before fun populateDb() {
        DatabaseHelper.repopulateDb()
        onView(isRoot()).perform(MainActivityTest.waitFor(1000))
    }

    @Test fun favoriteTest() {
        val recipeTab = "RECIPES"
        val favoriteTab = "FAVORITES"
        val name = "Best brownies"

        Thread.sleep(700)

        // Remove recipe from favorites
        onView(withText(favoriteTab))
                .perform(click())

        // Ensure no favorites text is not visible
        onView(withId(R.id.text_no_favorites))
                .check(matches(not(isDisplayed())))

        // Remove recipe from favorites
        onData(anything()).inAdapterView(withId(R.id.list_favorites)).atPosition(0)
                .perform(click())

        Thread.sleep(700)

        onView(withId(R.id.button_favorite))
                .perform(click())

        // Ensure no favorites text is visible
        Espresso.pressBack()

        Thread.sleep(700)

        onView(withId(R.id.text_no_favorites))
                .check(matches(isDisplayed()))

        // Now add a recipe to favorites
        onView(withText(recipeTab))
                .perform(click())

        onData(anything()).inAdapterView(withId(R.id.list_recipes_all)).atPosition(0)
                .perform(click())

        Thread.sleep(700)

        onView(withId(R.id.button_favorite))
                .perform(click())

        // Ensure recipe was added to favorites list
        Espresso.pressBack()

        Thread.sleep(700)

        onView(withText(favoriteTab))
                .perform(click())

        onData(anything()).inAdapterView(withId(R.id.list_favorites)).atPosition(0)
                .check(matches(hasDescendant(withText(name))))
    }
}
