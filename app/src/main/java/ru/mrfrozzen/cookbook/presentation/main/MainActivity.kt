package ru.mrfrozzen.cookbook.presentation.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import ru.mrfrozzen.cookbook.BuildConfig
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.presentation.about.AboutActivity
import ru.mrfrozzen.cookbook.presentation.edit.EditRecipeActivity
import ru.mrfrozzen.cookbook.presentation.view.ViewRecipeActivity
import ru.mrfrozzen.cookbook.utilities.*
import ru.mrfrozzen.cookbook.workers.DatabasePopulateWorker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), RecipeFragmentListener {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this,
                InjectorUtils.provideMainViewModelFactory(this)).get(MainViewModel::class.java)

        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        checkFirstRun()

        button_add.setOnClickListener { toggleButtons() }
        button_add_category.setOnClickListener { createCategory() }
        button_add_recipe.setOnClickListener { createRecipe() }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.setIconifiedByDefault(false)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                container.currentItem = 0
                val recipeFilterIntent = Intent(FILTER_RECIPE_ACTION)
                recipeFilterIntent.putExtra(INTENT_EXTRA_NAME_FILTER, newText)
                LocalBroadcastManager.getInstance(baseContext)
                        .sendBroadcast(recipeFilterIntent)
                return false
            }
        })

        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ADD_RECIPE, REQUEST_EDIT_RECIPE -> if (resultCode == RESULT_OK) {
                Snackbar.make(toolbar, R.string.alert_recipe_saved, Snackbar.LENGTH_LONG)
                        .show()
            }
            REQUEST_VIEW_RECIPE -> if (resultCode == RESULT_OK) {
                Snackbar.make(toolbar, R.string.alert_recipe_deleted, Snackbar.LENGTH_LONG)
                        .show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_search -> container.currentItem = 0
            R.id.action_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCategoryDelete(category: Category) {
        MaterialDialog(this)
                .message(text = getString(R.string.title_deleting_category, category.name))
                .positiveButton(R.string.action_delete) {
                    viewModel.deleteCategory(category)
                }
                .negativeButton(R.string.cancel)
                .show()
    }

    override fun onCategoryEdit(category: Category) {
        MaterialDialog(this)
                .message(R.string.title_edit_category)
                .input(hint = category.name, prefill = category.name, maxLength = CATEGORY_CHARACTER_LIMIT) { dialog, text ->
                    val name = text.toString()
                    if (name.isBlank()) {
                        Snackbar.make(toolbar, R.string.alert_blank_category_name, Snackbar.LENGTH_SHORT).show()
                    } else if (name.toLowerCase() == "Без категории" || name.toLowerCase() == "no category") {
                        Snackbar.make(toolbar, R.string.category_name_invalid, Snackbar.LENGTH_SHORT).show()
                    } else if (name.length > CATEGORY_CHARACTER_LIMIT)  {
                        Snackbar.make(toolbar, R.string.alert_too_long, Snackbar.LENGTH_SHORT).show()
                    } else {
                        // TODO: Check for duplicate category name, if needed
                        category.name = name.trim()
                        viewModel.updateCategory(category)
                        Snackbar.make(toolbar, R.string.alert_category_saved, Snackbar.LENGTH_SHORT).show()
                    }
                }
                .positiveButton(R.string.action_save)
                .negativeButton(R.string.cancel)
                .show()
    }

    override fun onRecipeEdit(id: Int) {
        val intent = Intent(this, EditRecipeActivity::class.java)
        intent.putExtra(INTENT_EXTRA_NAME_RECIPE_ID, id)
        intent.putExtra(INTENT_EXTRA_NAME_EDIT_MODE, RECIPE_EDIT_MODE_EDIT)
        startActivityForResult(intent, REQUEST_EDIT_RECIPE)
    }

    override fun onRecipeDelete(recipe: Recipe) {
        MaterialDialog(this)
                .message(text = getString(R.string.title_deleting_recipe, recipe.name))
                .positiveButton(R.string.action_delete) {
                    viewModel.deleteRecipe(recipe)
                    Snackbar.make(toolbar, R.string.alert_recipe_deleted, Snackbar.LENGTH_SHORT).show()
                }
                .negativeButton(R.string.cancel)
                .show()
    }

    override fun onRecipeSelected(id: Int) {
        val intent = Intent(this, ViewRecipeActivity::class.java)
        intent.putExtra(INTENT_EXTRA_NAME_RECIPE_ID, id)
        startActivityForResult(intent, REQUEST_VIEW_RECIPE)
    }

    override fun onRecipeShare(rwc: RecipeWithCategory) {
        val builder = StringBuilder()
        builder.append(rwc.recipe.name)
        builder.append("\n")
        builder.append("\n")
        if (rwc.category != null) {
            builder.append(rwc.category.name)
            builder.append("\n")
            builder.append("\n")
        }
        for (ingredient in rwc.recipe.ingredients) {
            builder.append(ingredient)
            builder.append("\n")
        }
        builder.append("\n")
        builder.append("\n")
        builder.append(rwc.recipe.instructions)
        val shareableRecipe = builder.toString()
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, shareableRecipe)
        intent.type = "text/plain"
        val title = resources.getString(R.string.action_share)
        val chooser = Intent.createChooser(intent, title)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        }
    }

    /**
     * Call this at first install, if example recipes where kept. This copies the example recipe
     * drawables into app storage, and then observes recipes from database, until the example recipes
     * are received. Then updates their image paths accordingly. Finally removes the observer.
     */
    private fun copyExampleRecipeImagesToStorage() {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val garraId = 1
        val desertId = 2
        val cakeId = 3
        val bananaId = 4
        val garraPath = storageDir.absolutePath + "/JPEG_garra.jpg"
        val desertPath = storageDir.absolutePath + "/JPEG_desert.jpg"
        val cakePath = storageDir.absolutePath + "/JPEG_cake.jpg"
        val bananaPath = storageDir.absolutePath + "/JPEG_banana.jpg"
        val garraFile = File(garraPath)
        val desertFile = File(desertPath)
        val cakeFile = File(cakePath)
        val bananaFile = File(bananaPath)
        val garraBitmap = BitmapFactory.decodeResource(resources, R.drawable.garra)
        val desertBitmap = BitmapFactory.decodeResource(resources, R.drawable.desert)
        val cakeBitmap = BitmapFactory.decodeResource(resources, R.drawable.cake)
        val bananaBitmap = BitmapFactory.decodeResource(resources, R.drawable.banana)
        FileOutputStream(garraFile).use { outputStream -> garraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) }
        FileOutputStream(desertFile).use { outputStream -> desertBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) }
        FileOutputStream(cakeFile).use { outputStream -> cakeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) }
        FileOutputStream(bananaFile).use { outputStream -> bananaBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) }
        val live = viewModel.recipesWithCategory
        live.observe(this, object : Observer<List<RecipeWithCategory>> {
            override fun onChanged(t: List<RecipeWithCategory>?) {
                if (t != null && t.size >= 4) {
                    live.removeObserver(this)
                    for (r in t) {
                        if (r.recipe.id == garraId) {
                            r.recipe.imagePath = garraPath
                            viewModel.updateRecipe(r.recipe)
                        } else if (r.recipe.id == cakeId) {
                            r.recipe.imagePath = cakePath
                            viewModel.updateRecipe(r.recipe)
                        } else if (r.recipe.id == desertId) {
                            r.recipe.imagePath = desertPath
                            viewModel.updateRecipe(r.recipe)
                        }else if (r.recipe.id == bananaId) {
                            r.recipe.imagePath = bananaPath
                            viewModel.updateRecipe(r.recipe)
                        }
                    }
                }
            }
        })
    }

    private fun toggleButtons() {
        /*
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null) {
            val list = storageDir.listFiles()
            var count = 0
            for (file in list) {
                val name = file.name
                Log.e(TAG, name)
                count++
            }
            Log.e(TAG, "Count: $count")
        }
        */
        val fabOpen = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_open)
        val fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close)
        val rotateForward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_forward)
        val rotateBackward = AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_backward)
        val isFabOpen = button_add_recipe.isVisible

        if (isFabOpen) {
            button_add.startAnimation(rotateBackward)
            text_add_recipe.startAnimation(fabClose)
            text_add_category.startAnimation(fabClose)
            button_add_recipe.startAnimation(fabClose)
            button_add_category.startAnimation(fabClose)
            button_add_recipe.isClickable = false
            button_add_category.isClickable = false
            button_add_recipe.isVisible = false
            button_add_category.isVisible = false
        } else {
            button_add.startAnimation(rotateForward)
            text_add_recipe.startAnimation(fabOpen)
            text_add_category.startAnimation(fabOpen)
            button_add_recipe.startAnimation(fabOpen)
            button_add_category.startAnimation(fabOpen)
            button_add_recipe.isClickable = true
            button_add_category.isClickable = true
            button_add_recipe.isVisible = true
            button_add_category.isVisible = true
        }
    }

    private fun createCategory() {
        toggleButtons()
        MaterialDialog(this)
                .message(R.string.title_create_category)
                .input(hintRes = R.string.category_name_hint, maxLength = CATEGORY_CHARACTER_LIMIT) { _, text ->
                    val name = text.toString()
                    if (name.isBlank()) {
                        Snackbar.make(toolbar, R.string.alert_blank_category_name, Snackbar.LENGTH_SHORT).show()
                    } else if (name.toLowerCase() == "Без категории" || name.toLowerCase() == "no category") {
                        Snackbar.make(toolbar, R.string.category_name_invalid, Snackbar.LENGTH_SHORT).show()
                    } else if (name.length > CATEGORY_CHARACTER_LIMIT)  {
                        Snackbar.make(toolbar, R.string.alert_too_long, Snackbar.LENGTH_SHORT).show()
                    } else {
                        // TODO: Check for duplicate category name, if needed
                        viewModel.insertCategory(Category(0, name.trim()))
                        Snackbar.make(toolbar, R.string.alert_category_saved, Snackbar.LENGTH_SHORT).show()
                    }
                }
                .positiveButton(R.string.action_create)
                .negativeButton(R.string.cancel)
                .show()
    }

    private fun createRecipe() {
        toggleButtons()
        val intent = Intent(this, EditRecipeActivity::class.java)
        intent.putExtra(INTENT_EXTRA_NAME_EDIT_MODE, RECIPE_EDIT_MODE_CREATE)
        startActivityForResult(intent, REQUEST_ADD_RECIPE)
    }

    private fun checkFirstRun() {
        val currentVersionCode = BuildConfig.VERSION_CODE

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOES_NOT_EXIST)

        if (currentVersionCode == savedVersionCode) {
            return
        } else if (savedVersionCode == DOES_NOT_EXIST) {
            // First install, populate db. When work request is finished, copy images to storage
            val request = OneTimeWorkRequestBuilder<DatabasePopulateWorker>().build()
            val workManager = WorkManager.getInstance()
            workManager.enqueue(request)
            val status = workManager.getWorkInfoByIdLiveData(request.id)
            status.observe(this, Observer { workStatus ->
                if (workStatus != null && workStatus.state.isFinished) copyExampleRecipeImagesToStorage()
            })
        }/* else if (currentVersionCode > savedVersionCode) {
            // Upgrade
        }*/

        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply()
    }

    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> FavoriteRecipesFragment.newInstance()
                2 -> CategoriesFragment.newInstance()
                else -> AllRecipesFragment.newInstance()
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.name
    }
}
