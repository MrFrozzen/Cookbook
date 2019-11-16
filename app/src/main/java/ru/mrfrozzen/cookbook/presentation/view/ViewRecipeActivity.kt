package ru.mrfrozzen.cookbook.presentation.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.presentation.edit.EditRecipeActivity
import ru.mrfrozzen.cookbook.utilities.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_view_recipe.*
import kotlinx.android.synthetic.main.content_view_recipe.*

class ViewRecipeActivity : AppCompatActivity() {
    private val TAG by lazy { ViewRecipeActivity::class.java.simpleName }

    private lateinit var viewModel: ViewRecipeViewModel
    private var recipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val factory = InjectorUtils.provideViewRecipeViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(ViewRecipeViewModel::class.java)

        val id = intent.getIntExtra(INTENT_EXTRA_NAME_RECIPE_ID, DOES_NOT_EXIST)
        if (id == DOES_NOT_EXIST) throw Exception("No recipe id given as intent extra")

        button_favorite.setOnClickListener { toggleFavorite() }

        subscribeUi(id)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_view_recipe, menu)
        return true
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_EDIT_RECIPE -> if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(text_view_category, R.string.alert_recipe_saved, Snackbar.LENGTH_SHORT).show()
                val id = data?.getIntExtra(INTENT_EXTRA_NAME_RECIPE_ID, DOES_NOT_EXIST)
                if (id != null && id != DOES_NOT_EXIST) subscribeUi(id)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_delete_recipe -> {
                MaterialDialog(this)
                        .message(R.string.alert_delete_recipe)
                        .positiveButton(R.string.action_delete) {
                            val r = recipe
                            if (r != null) {
                                viewModel.deleteRecipe(r)
                                setResult(RESULT_OK, Intent())
                                finish()
                            }
                        }
                        .negativeButton(R.string.cancel)
                        .show()
                return true
            }

            R.id.action_edit_recipe -> {
                val r = recipe ?: return true
                val intent = Intent(this, EditRecipeActivity::class.java)
                intent.putExtra(INTENT_EXTRA_NAME_RECIPE_ID, r.id)
                intent.putExtra(INTENT_EXTRA_NAME_EDIT_MODE, RECIPE_EDIT_MODE_EDIT)
                startActivityForResult(intent, REQUEST_EDIT_RECIPE)
                return true
            }
            R.id.action_share -> {
                shareRecipe()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun toggleFavorite() {
        val r = recipe ?: return
        if (r.favorite == 1) {
            r.favorite = 0
            button_favorite.setImageResource(R.drawable.ic_favorite_border_white)
            Snackbar.make(toolbar, R.string.alert_recipe_removed_from_favorites, Snackbar.LENGTH_SHORT).show()
        } else {
            r.favorite = 1
            button_favorite.setImageResource(R.drawable.ic_favorite_white)
            Snackbar.make(toolbar, R.string.alert_recipe_added_to_favorites, Snackbar.LENGTH_SHORT).show()
        }
        viewModel.updateRecipe(r)
    }

    private fun shareRecipe() {
        val r = recipe ?: return
        val builder = StringBuilder()
        builder.append(r.name)
        builder.append("\n")
        builder.append("\n")
        if (r.categoryId != null) {
            builder.append(text_view_category.text.toString())
            builder.append("\n")
            builder.append("\n")
        }
        builder.append(text_view_ingredients.text.toString())
        builder.append("\n")
        builder.append("\n")
        builder.append(text_view_instructions.text.toString())
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

    private fun subscribeUi(id: Int) {
        viewModel.getRecipeWithCategory(id).observe(this, Observer { rwc ->
            if (rwc != null) {
                recipe = rwc.recipe
                val r = rwc.recipe
                toolbar_layout.title = r.name
                if (rwc.category == null) {
                    text_view_category.visibility = GONE
                } else {
                    text_view_category.visibility = VISIBLE
                    text_view_category.text = rwc.category.name
                }
                if (r.imagePath != null) {
                    val path = r.imagePath
                    if (path != null) food_image_view.doOnLayout { ImageUtils.setImage(food_image_view, path) }
                } else {
                    food_image_view.setImageResource(android.R.color.transparent)
                }
                text_view_ingredients.text = ""
                for (i in 0 until r.ingredients.size) {
                    text_view_ingredients.append(r.ingredients[i] + "\n")
                }
                text_view_instructions.text = r.instructions
                if (r.favorite == 1) {
                    button_favorite.setImageResource(R.drawable.ic_favorite_white)
                }
            }
        })
    }
}
