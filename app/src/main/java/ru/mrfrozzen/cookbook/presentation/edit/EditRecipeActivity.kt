package ru.mrfrozzen.cookbook.presentation.edit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.*
import android.widget.EditText
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.utilities.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit_recipe.*
import kotlinx.android.synthetic.main.content_edit_recipe.*
import java.io.*
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.*

class EditRecipeActivity : AppCompatActivity() {
    private val TAG by lazy { EditRecipeActivity::class.java.simpleName }

    private var ingredientAlertDismissed = false
    private var adapter: CategorySpinnerAdapter? = null
    private lateinit var viewModel: EditRecipeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val factory = InjectorUtils.provideEditRecipeViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(EditRecipeViewModel::class.java)

        // Get the recipe edit mode from intent extra: either creating a new recipe or editing
        // an existing recipe.
        val editMode = intent.getIntExtra(INTENT_EXTRA_NAME_EDIT_MODE, -1)
        if (editMode == -1) {
            throw InvalidParameterException("No edit mode intent extra (name: "
                    + INTENT_EXTRA_NAME_EDIT_MODE + ") given for "
                    + EditRecipeActivity::class.java.name)
        } else if (editMode != RECIPE_EDIT_MODE_EDIT && editMode != RECIPE_EDIT_MODE_CREATE) {
            throw InvalidParameterException("Invalid edit mode intent extra (name: "
                    + INTENT_EXTRA_NAME_EDIT_MODE + ") given for "
                    + EditRecipeActivity::class.java.name + ". Should be one of ["
                    + RECIPE_EDIT_MODE_EDIT + ", " + RECIPE_EDIT_MODE_CREATE + "], but was: "
                    + editMode)
        }
        viewModel.mode = editMode

        ingredientAlertDismissed = checkPrefHideIngredientDialog()

        if (savedInstanceState == null) {
            if (editMode == RECIPE_EDIT_MODE_CREATE) {

                /* CREATE NEW RECIPE */

                // Set activity title and spawn a single empty ingredient row.
                toolbar_layout.title = resources.getString(R.string.title_activity_create_recipe)
                initCategories()
                newRow()

            } else {

                /* EDIT RECIPE */

                // Get edited recipe and it's category via observe, and cache to ViewModel
                val id = intent.getIntExtra(INTENT_EXTRA_NAME_RECIPE_ID, DOES_NOT_EXIST)
                if (id == DOES_NOT_EXIST) throw Exception("Mode is EDIT, but no Recipe id given!")
                fetchEditedRecipeFromDb(id)
            }
        } else {
            // SavedInstanceState is not null, so get Recipe info from it (orientation change).
            // This works the same when creating a new recipe and editing a recipe.
            val name = savedInstanceState.getString(STATE_NAME)

            val categoryEnabled = savedInstanceState.getBoolean(STATE_CATEGORY_ENABLED)
            viewModel.isCategoryEnabled = categoryEnabled

            val spinnerIndex = savedInstanceState.getInt(STATE_CATEGORY)
            if (spinnerIndex != INVALID_POSITION) viewModel.selectedCategorySpinnerIndex = spinnerIndex

            val ingredients = savedInstanceState.getStringArrayList(STATE_INGREDIENTS)
            val instructions = savedInstanceState.getString(STATE_INSTRUCTIONS)
            //Log.i(TAG, "Populating views with instance state")
            if (name != null && ingredients != null && instructions != null)
                populateViews(name, ingredients, instructions)
        }

        button_take_photo.setOnClickListener { editImage() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_recipe, menu)
        return true
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        //Log.i(TAG, "Saving instance state")
        savedInstanceState.putString(STATE_NAME, edit_name.text.toString().trim { it <= ' ' })
        savedInstanceState.putInt(STATE_CATEGORY, spinner_category.selectedItemPosition)
        savedInstanceState.putBoolean(STATE_CATEGORY_ENABLED, checkbox_category.isEnabled)
        savedInstanceState.putString(STATE_INSTRUCTIONS, edit_instructions.text.toString().trim { it <= ' ' })
        val ingredients = ArrayList<String>()
        for (i in 0 until table_ingredients.childCount) {
            val view = table_ingredients.getChildAt(i)
            val row = view as TableRow
            if ((row.getChildAt(0) as EditText).text.toString().isBlank()) {
                continue
            }
            ingredients.add((row.getChildAt(0) as EditText).text.toString().trim { it <= ' ' })
        }
        savedInstanceState.putStringArrayList(STATE_INGREDIENTS, ingredients)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                cycleImages()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The picture taking was cancelled, but because the placeholder file was already
                // created, we must delete it.
                deleteImage(viewModel.imagePlaceholderFile)
                viewModel.imagePlaceholderFile = null
            }
        } else if (requestCode == REQUEST_IMAGE_PICK) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    //Log.e(TAG, "Error picking image, received null data")
                    return
                }
                try {
                    val d = data.data
                    if (d != null) {
                        val inputStream = contentResolver.openInputStream(d)
                        if (inputStream != null) {
                            copyPickedImageToAppDir(inputStream)
                            cycleImages()
                        }
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
    }

    override fun onBackPressed() {
        val mode = viewModel.mode
        if (mode == RECIPE_EDIT_MODE_EDIT) {
            val intent = Intent()
            val r = viewModel.editedRecipe ?: return super.onBackPressed()
            intent.putExtra(INTENT_EXTRA_NAME_RECIPE_ID, r.id)
            setResult(Activity.RESULT_CANCELED, intent)
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_save) {
            val mode = viewModel.mode
            if (mode == RECIPE_EDIT_MODE_EDIT) {
                updateEditedRecipe()
            } else if (mode == RECIPE_EDIT_MODE_CREATE) {
                insertRecipe()
            }
            return true
        } else if (id == android.R.id.home) {
            val mode = viewModel.mode
            if (mode == RECIPE_EDIT_MODE_EDIT) {
                val intent = Intent()
                val r = viewModel.editedRecipe ?: return super.onOptionsItemSelected(item)
                intent.putExtra(INTENT_EXTRA_NAME_RECIPE_ID, r.id)
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // NOT an orientation change; activity is being destroyed for good.
            // We should delete all images, that are not in use by the currently edited recipe.
            deleteCachedImagesExcept(viewModel.editedRecipeImagePath)
        }
    }

    private fun cycleImages() {
        if (viewModel.imagePlaceholderFile != null) {
            // Delete currently visible image, if it is not the saved image of currently
            // edited recipe.
            val currentlyVisible = viewModel.currentVisibleImageFile
            val saved = viewModel.editedRecipeImagePath
            if (currentlyVisible != null && currentlyVisible != saved) {
                deleteImage(viewModel.currentVisibleImageFile)
                viewModel.currentVisibleImageFile = null
            }
            val f = viewModel.imagePlaceholderFile
            if (f != null) {
                onImageViewLoadSetImage(f)
                viewModel.imagePlaceholderFile = null
            }
        }
    }

    /**
     * Return true if ingredient row deletion alerts should not be shown.
     */
    private fun checkPrefHideIngredientDialog(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(PREF_INGREDIENT_DELETE_DISMISS, false)
    }

    private fun clearRecipeImage() {
        deleteCachedImagesExcept(viewModel.editedRecipeImagePath)
        viewModel.currentVisibleImageFile = null
        viewModel.imageCleared = true
        setRecipeImage()
    }

    /**
     * Writes from InputStream to an image file in the app folder got via nextImageFile().
     * @param inputStream The InputStream that will be written to a file got from nextImageFile().
     */
    private fun copyPickedImageToAppDir(inputStream: InputStream) {
        var file: File? = null
        try {
            file = nextImageFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (file != null) {
            try {
                // TODO: Compress/scale down copied image?
                FileOutputStream(file).use { outputStream -> inputStream.copyTo(outputStream) }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * Delete the cached files held by the ViewModel. Deletes both the temp file, and currently
     * visible file, which may be used by the currently edited recipe, if we are in edit mode.
     */
    private fun deleteCachedImages() {
        var path = viewModel.imagePlaceholderFile
        if (path != null) {
            //Log.i(TAG, "Deleting placeholder image file: $path")
            deleteImage(path)
        } else {
            //Log.i(TAG, "Placeholder image file is null, not deleting.")
        }

        path = viewModel.currentVisibleImageFile
        if (path != null) {
            //Log.i(TAG, "Deleting currently visible file: $path")
            deleteImage(path)
        } else {
            //Log.i(TAG, "Currently visible image file is null, not deleting.")
        }
    }

    /**
     * Handles removing an ingredient row or showing the ingredient deletion dialog.
     */
    fun deleteIngredient(view: View) {
        if (ingredientAlertDismissed) {
            removeRow()
        } else if (table_ingredients.childCount > 1) {
            MaterialDialog(this)
                    .message(R.string.alert_delete_ingredient)
                    .checkBoxPrompt(R.string.title_dont_show_dialog) {}
                    .positiveButton(R.string.action_delete) { dialog ->
                        val isChecked = dialog.isCheckPromptChecked()
                        if (isChecked) writePrefHideIngredientDialog()
                        removeRow()
                    }
                    .negativeButton(R.string.cancel)
                    .show()
        }
    }

    /**
     * Delete an image (or any file).
     *
     * @param path Path to the file that will be deleted.
     */
    private fun deleteImage(path: String?) {
        if (path != null) {
            val file = File(path)
            val deleted = file.delete()
            /*
            if (!deleted)
                Log.i(TAG, "Could not delete file: " + path
                        + ". It is possibly already deleted.")
            else
                Log.i(TAG, "Deleted file: $path")
                */
        } else {
            //Log.w(TAG, "Cannot delete image, path is null!")
        }
    }

    /**
     * Delete both the temp image file and currently visible image file, if they are not equal to
     * the given String parameter.
     *
     * @param except If the file to be deleted is equal to this, it won't be deleted.
     */
    private fun deleteCachedImagesExcept(except: String?) {
        if (except == null) {
            deleteCachedImages()
        } else {
            var path = viewModel.imagePlaceholderFile
            if (path != null && except != path) {
                //Log.i(TAG, "Deleting placeholder image file: $path")
                deleteImage(path)
            }
            path = viewModel.currentVisibleImageFile
            if (path != null && except != path) {
                //Log.i(TAG, "Deleting currently visible file: $path")
                deleteImage(path)
            }
        }
    }

    /**
     * Start image capturing intent, with an empty file as the image output.
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = nextImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    /**
     * Start image select intent.
     */
    private fun dispatchSelectImageIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun editImage() {
        // TODO: material design simple dialog
        MaterialDialog(this)
                .title(R.string.title_edit_image)
                .listItemsSingleChoice(R.array.image_options) { _, index, _ ->
                    when (index) {
                        0 -> dispatchTakePictureIntent()
                        1 -> dispatchSelectImageIntent()
                        2 -> clearRecipeImage()
                    }
                }
                //.positiveButton(R.string.action_choose)
                .show()
    }

    /**
     * Get the edited recipe and category from database.
     * Remove observer as soon as non-null data is observed.
     */
    private fun fetchEditedRecipeFromDb(id: Int) {
        val live = viewModel.getRecipeWithCategory(id)
        live.observe(this, object : Observer<RecipeWithCategory> {
            override fun onChanged(t: RecipeWithCategory?) {
                if (t != null) {
                    live.removeObserver(this)
                    viewModel.editedRecipe = t.recipe
                    viewModel.editedRecipeImagePath = t.recipe.imagePath
                    viewModel.editedRecipeCategory = t.category
                    if (t.category != null) viewModel.isCategoryEnabled = true
                    populateViews(t.recipe)
                }
            }
        })
    }

    /**
     * Initialize the category checkbox and spinner, and cache categories to ViewModel.
     */
    private fun initCategories() {
        val live = viewModel.categories
        live.observe(this, object : Observer<List<Category>> {
            override fun onChanged(data: List<Category>?) {
                if (data != null) {
                    live.removeObserver(this)
                    if (data.isEmpty()) {
                        checkbox_category.isChecked = false
                        checkbox_category.isEnabled = false
                        spinner_category.visibility = GONE
                    } else {
                        checkbox_category.isEnabled = true
                    }
                    adapter = CategorySpinnerAdapter(this@EditRecipeActivity, data)
                    spinner_category.adapter = adapter
                    val isCategoryEnabled = viewModel.isCategoryEnabled
                    checkbox_category.isChecked = isCategoryEnabled
                    if (isCategoryEnabled) spinner_category.visibility = VISIBLE else spinner_category.visibility = GONE
                    if (isCategoryEnabled) {
                        val index = viewModel.selectedCategorySpinnerIndex
                        val editedCategory = viewModel.editedRecipeCategory
                        if (index != INVALID_POSITION) {
                            spinner_category.setSelection(index)
                        } else if (editedCategory != null) {
                            val cats = adapter?.items
                            val a = adapter
                            if (a != null && cats != null) {
                                for (c in cats) {
                                    if (c.id == editedCategory.id) {
                                        spinner_category.setSelection(a.getPosition(c))
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
        checkbox_category.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                spinner_category.visibility = VISIBLE
            } else {
                spinner_category.visibility = GONE
            }
        }
    }

    /**
     * Insert a new recipe to database, with it's data taken from the Views.
     */
    private fun insertRecipe() {
        if (edit_name.text.toString().isBlank()) {
            Snackbar.make(edit_name, R.string.alert_blank_recipe_name, Snackbar.LENGTH_SHORT).show()
        } else {
            val recipe = newRecipeFromViews()
            viewModel.editedRecipeImagePath = recipe.imagePath
            viewModel.insertRecipe(recipe)
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
    }

    /**
     * Create and return a new (empty) image file, on top of which the real image will be saved.
     *
     *
     * Creates the file name with current timestamp. Deletes the previous recipe image, sets the
     * current image file as the previous, and then creates the new file as the current name. If one
     * of the cached images
     *
     * @return The created empty file.
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun nextImageFile(): File {
        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        // Delete previous placeholder file if it exists
        deleteImage(viewModel.imagePlaceholderFile)

        // Set new placeholder file
        viewModel.imagePlaceholderFile = image.absolutePath
        return image
    }

    /**
     * This gets called when tapping the add ingredient button. OnClick is set in XML, so this takes
     * a View parameter. Focus is requested to the added row.
     */
    fun newRow(view: View) {
        newRow(requestFocus = true)
    }

    /**
     * Append a new ingredient row to the ingredient table. If requestFocus is given and it's True,
     * then focus will be requested by the new row.
     */
    private fun newRow(requestFocus: Boolean = false) {
        // Table row
        val row = TableRow(this)
        var lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
        lp.setMargins(16, 16, 16, 16)
        row.layoutParams = lp

        // Ingredient EditText
        val ingredient = EditText(this)
        lp = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.6f)
        ingredient.layoutParams = lp
        ingredient.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
        ingredient.maxLines = 2
        row.addView(ingredient)
        if (requestFocus) ingredient.requestFocus()
        table_ingredients.addView(row)
    }

    /**
     * Sets the recipe ImageView's image to the Bitmap pointed at by the given path, when the
     * ImageView has gotten it's layout. Sets the current visible image for ViewModel.
     *
     * @param path Path to the Bitmap to be set as the image for recipe ImageView.
     */
    private fun onImageViewLoadSetImage(path: String) {
        if (food_image_view.height == 0 || food_image_view.width == 0) {
            food_image_view.doOnLayout {
                ImageUtils.setImage(food_image_view, path)
                viewModel.currentVisibleImageFile = path
                viewModel.imageCleared = false
            }
        } else {
            ImageUtils.setImage(food_image_view, path)
            viewModel.currentVisibleImageFile = path
            viewModel.imageCleared = false
        }
    }

    /**
     * Populate Views with data from given recipe.
     *
     * @param recipe The recipe which's data will be set to the Views.
     */
    internal fun populateViews(recipe: Recipe) {
        edit_name.setText(recipe.name)
        edit_instructions.setText(recipe.instructions)
        val ingredients = recipe.ingredients
        for (i in ingredients.indices) {
            var lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
            lp.setMargins(16, 16, 16, 16)
            val row = TableRow(this)
            row.layoutParams = lp
            val ingredient = EditText(this)
            lp = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.2f)
            ingredient.layoutParams = lp
            ingredient.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
            ingredient.maxLines = 2
            ingredient.setText(ingredients[i])
            row.addView(ingredient)
            table_ingredients.addView(row)
        }

        initCategories()

        // If there are no ingredients, create one empty row.
        if (table_ingredients.childCount == 0) newRow()

        setRecipeImage()
    }

    /**
     * Populate Views with given parameters.
     *
     * @param name         Recipe name.
     * @param ingredients  List of recipe ingredients.
     * @param instructions Recipe instructions.
     */
    private fun populateViews(name: String, ingredients: List<String>, instructions: String) {
        edit_name.setText(name)
        edit_instructions.setText(instructions)
        for (i in ingredients.indices) {
            var lp = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT)
            lp.setMargins(16, 16, 16, 16)
            val row = TableRow(this)
            row.layoutParams = lp
            val ingredient = EditText(this)
            lp = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.2f)
            ingredient.layoutParams = lp
            ingredient.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(50))
            ingredient.maxLines = 2
            ingredient.setText(ingredients[i])
            row.addView(ingredient)
            table_ingredients.addView(row)
        }

        initCategories()

        // If there are no ingredients, create one empty row.
        if (table_ingredients.childCount == 0) newRow()

        setRecipeImage()
    }

    /**
     * Delete the lowest ingredient row in the ingredient table, if it is not the last row.
     */
    private fun removeRow() {
        val numberOfRows = table_ingredients.childCount
        if (numberOfRows > 1) table_ingredients.removeViewAt(numberOfRows - 1)
    }

    /**
     * Creates and returns a new Recipe with it's data fetched from the Views.
     */
    private fun newRecipeFromViews(): Recipe {
        val name = edit_name.text.toString().trim()
        var categoryId: Int? = null
        if (checkbox_category.isChecked) {
            val category = spinner_category.selectedItem as Category
            categoryId = category.id
        }
        val instructions = edit_instructions.text.toString().trim()
        val ingredients = ArrayList<String>()
        for (i in 0 until table_ingredients.childCount) {
            val view = table_ingredients.getChildAt(i)
            val row = view as TableRow
            if ((row.getChildAt(0) as EditText).text.toString().isBlank()) {
                continue
            }
            ingredients.add((row.getChildAt(0) as EditText).text.toString().trim())
        }
        val imagePath = viewModel.currentVisibleImageFile
        return Recipe(0, name, categoryId, ingredients, instructions, 0, imagePath)
    }

    private fun setRecipeImage() {
        // If current visible image is not null, set that as the image.
        // Otherwise set the currently edited recipe's image, if available.
        // TODO: functionality for clearing the image -> in that case don't set any image even if currently edited recipe's image is available.
        val c = viewModel.currentVisibleImageFile
        val e = viewModel.editedRecipeImagePath
        val cleared = viewModel.imageCleared
        if (c != null) {
            onImageViewLoadSetImage(c)
        } else if (!cleared && e != null) {
            onImageViewLoadSetImage(e)
        } else if (cleared) {
            food_image_view.setImageResource(android.R.color.transparent)
        }
    }

    private fun Recipe.updateRecipeFromViews() {
        this.name = edit_name.text.toString().trim()
        if (checkbox_category.isChecked) {
            val category = spinner_category.selectedItem as Category
            this.categoryId = category.id
        }
        this.instructions = edit_instructions.text.toString().trim()
        val ingredients = ArrayList<String>()
        for (i in 0 until table_ingredients.childCount) {
            val view = table_ingredients.getChildAt(i)
            val row = view as TableRow
            if ((row.getChildAt(0) as EditText).text.toString().isBlank()) {
                continue
            }
            ingredients.add((row.getChildAt(0) as EditText).text.toString().trim())
        }
        this.ingredients = ingredients
        this.imagePath = viewModel.currentVisibleImageFile
    }

    /**
     * Update the edited recipe with new values, then finish the activity.
     *
     * Get new recipe field values from the Views, and the new image path from getCurrentImage.
     * Also delete possible unused images.
     */
    private fun updateEditedRecipe() {
        if (edit_name.text.toString().isBlank()) {
            Snackbar.make(edit_name, R.string.alert_blank_recipe_name, Snackbar.LENGTH_SHORT).show()
        } else {
            val r = viewModel.editedRecipe ?: return
            r.updateRecipeFromViews()
            if (checkbox_category.isChecked) {
                val category = spinner_category.selectedItem as Category
                r.categoryId = category.id

            } else {
                r.categoryId = null
            }

            // If image was cleared, delete all cached images and possible already existing recipe image
            if (viewModel.imageCleared) {
                deleteCachedImages()
                deleteImage(viewModel.editedRecipeImagePath)
            } else {

                // If the previous saved recipe image is different from the new, delete the old one.
                val editedImage = viewModel.editedRecipeImagePath
                if (editedImage != null && editedImage != r.imagePath) {
                    deleteImage(editedImage)
                }
            }

            // Set the updated recipe image as the editedRecipeImage, so that it won't get deleted
            // in onDestroy().
            viewModel.editedRecipeImagePath = r.imagePath
            viewModel.updateRecipe(r)
            val intent = Intent()
            intent.putExtra(INTENT_EXTRA_NAME_RECIPE_ID, r.id)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    /**
     * Write the SharedPreference indicating the alert dialog when deleting ingredients shall not
     * be shown anymore.
     */
    private fun writePrefHideIngredientDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // TODO: test
        //prefs.edit().putBoolean(PREF_INGREDIENT_DELETE_DISMISS, true).apply()
        prefs.edit { putBoolean(PREF_INGREDIENT_DELETE_DISMISS, true) }
        ingredientAlertDismissed = true
    }

    companion object {
        private const val STATE_NAME = "name"
        private const val STATE_CATEGORY = "category"
        private const val STATE_CATEGORY_ENABLED = "categoryEnabled"
        private const val STATE_INGREDIENTS = "ingredients"
        private const val STATE_INSTRUCTIONS = "instructions"
    }
}
