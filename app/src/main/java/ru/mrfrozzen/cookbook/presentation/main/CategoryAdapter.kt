package ru.mrfrozzen.cookbook.presentation.main


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe

class CategoryAdapter internal constructor(private val context: Context, private val groups: List<Category>, private val data: Map<Category, List<Recipe>>) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return groups.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return data[groups[groupPosition]]!!.size
    }

    override fun getGroup(groupPosition: Int): Category {
        return groups[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Recipe {
        return data[groups[groupPosition]]!![childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?,
                              parent: ViewGroup): View? {
        var cv = convertView
        if (cv == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = layoutInflater.inflate(R.layout.item_category_group, null)
        }
        var categoryText: TextView? = null
        if (cv != null) {
            categoryText = cv.findViewById(R.id.text_category_group)
        }
        if (categoryText != null) {
            categoryText.text = getGroup(groupPosition).name
        }
        return cv
    }

    @SuppressLint("InflateParams")
    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                              convertView: View?, parent: ViewGroup): View? {
        var cv = convertView
        if (cv == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            cv = layoutInflater.inflate(R.layout.item_exp_recipe, null)
        }
        var recipeText: TextView? = null
        if (cv != null) {
            recipeText = cv.findViewById(R.id.text_expanded_recipe)
        }
        if (recipeText != null) {
            recipeText.text = getChild(groupPosition, childPosition).name
        }
        return cv
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
