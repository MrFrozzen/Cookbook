package ru.mrfrozzen.cookbook.presentation.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import java.util.*

class RecipeAdapter internal constructor(context: Context, private val recipesWithCategory: List<RecipeWithCategory>) : ArrayAdapter<RecipeWithCategory>(context, 0, recipesWithCategory), Filterable {
    private var filtered: List<RecipeWithCategory> = recipesWithCategory
    private val filter = ItemFilter()

    init {
        filtered = recipesWithCategory
    }

    override fun getCount(): Int {
        return filtered.size
    }

    override fun getItem(position: Int): RecipeWithCategory? {
        return filtered[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return filter
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cv: View

        val recipe = getItem(position)

        val viewHolder: ViewHolder
        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            cv = inflater.inflate(R.layout.item_recipe, parent, false)
            val name: TextView = cv.findViewById(R.id.text_recipe_name)
            val category: TextView = cv.findViewById(R.id.text_recipe_category)
            viewHolder = ViewHolder(name, category)
            cv.tag = viewHolder
        } else {
            cv = convertView
            viewHolder = cv.tag as ViewHolder
        }
        if (recipe != null) {
            viewHolder.name.text = recipe.recipe.name
            if (recipe.category == null) {
                viewHolder.category.setText(R.string.recipe_no_category)
            } else {
                viewHolder.category.text = recipe.category.name
            }
        }
        return cv
    }

    private class ViewHolder(val name: TextView, val category: TextView)

    private inner class ItemFilter : Filter() {
        override fun performFiltering(constraint: CharSequence): Filter.FilterResults {
            val filterString = constraint.toString().toLowerCase()
            val results = Filter.FilterResults()
            val original = recipesWithCategory
            val count = original.size
            val newList = ArrayList<RecipeWithCategory>(count)
            var filterableString: String?
            for (i in 0 until count) {
                // Check if match found in recipe name
                val rwc = original[i]
                filterableString = rwc.recipe.name
                if (filterableString.toLowerCase().contains(filterString)) {
                    newList.add(rwc)
                } else if (rwc.category != null) { // if no recipe name match, check if match in category name
                    filterableString = rwc.category.name
                    if (filterableString.toLowerCase().contains(filterString)) {
                        newList.add(original[i])
                    }
                }
            }
            results.values = newList
            results.count = newList.size
            return results
        }

        override fun publishResults(constraint: CharSequence, results: Filter.FilterResults) {
            filtered = results.values as ArrayList<RecipeWithCategory>
            notifyDataSetChanged()
        }
    }

}