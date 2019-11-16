package ru.mrfrozzen.cookbook.suite

import ru.mrfrozzen.cookbook.unit.DatabaseMigrationTest
import ru.mrfrozzen.cookbook.unit.DatabaseTest
import org.junit.runner.RunWith
import org.junit.runners.Suite


@RunWith(Suite::class)
@Suite.SuiteClasses(DatabaseTest::class, DatabaseMigrationTest::class)
class UnitTestSuite
