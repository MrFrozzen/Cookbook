package ru.mrfrozzen.cookbook.utilities

const val DB_NAME = "cookbook_db"
const val RECIPE_DATA_FILENAME = "recipes.json"
const val CATEGORY_DATA_FILENAME = "categories.json"
const val FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider"
const val PREFS_NAME = "ru.mrfrozzen.cookbook"
const val PREF_INGREDIENT_DELETE_DISMISS = "ingredient_delete_dismiss"
const val FILTER_RECIPE_ACTION = "ru.mrfrozzen.cookbook.FILTER_RECIPE"
const val PREF_VERSION_CODE_KEY = "version_code"
const val CATEGORY_CHARACTER_LIMIT = 25
const val DOES_NOT_EXIST = -1


// Requests
const val REQUEST_ADD_RECIPE = 1
const val REQUEST_EDIT_RECIPE = 2
const val REQUEST_VIEW_RECIPE = 3
const val REQUEST_IMAGE_CAPTURE = 4
const val REQUEST_IMAGE_PICK = 7

// Intent extra names
const val INTENT_EXTRA_NAME_RECIPE_ID = "recipeId"
const val INTENT_EXTRA_NAME_FILTER = "filter"
const val INTENT_EXTRA_NAME_EDIT_MODE = "editMode"

// Intent extra values for editMode
const val RECIPE_EDIT_MODE_EDIT = 5
const val RECIPE_EDIT_MODE_CREATE = 6
