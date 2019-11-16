package ru.mrfrozzen.cookbook.suite

import ru.mrfrozzen.cookbook.ui.CreateRecipeTest
import ru.mrfrozzen.cookbook.ui.FavoriteTest
import ru.mrfrozzen.cookbook.ui.MainActivityTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(MainActivityTest::class, FavoriteTest::class, CreateRecipeTest::class)
class UiTestSuite