package ru.mrfrozzen.cookbook.presentation.edit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.db.entity.Category

class CategorySpinnerAdapter(context: Context, val items: List<Category>) : ArrayAdapter<Category>(context, 0, items) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val name = TextView(context)
        name.setPadding(16, 16, 16, 16)
        name.text = items[position].name
        return name
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cw = convertView
        // Get recipe item for this position
        val category = getItem(position)

        // Check if an existing view is being reused, otherwise inflate view
        val viewHolder: ViewHolder // view lookup cache stored in tag
        if (cw == null) {

            // No view to reuse, inflate new view
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(context)
            cw = inflater.inflate(R.layout.item_spinner_category, parent, false)
            viewHolder.name = cw!!.findViewById(R.id.text_category_spinner)

            // Cache the viewHolder object inside the fresh view
            cw.tag = viewHolder
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = cw.tag as ViewHolder
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        if (category != null) {
            viewHolder.name!!.text = category.name
        }

        // Return the completed view to render on screen
        return cw
    }

    // View lookup cache
    private class ViewHolder {
        internal var name: TextView? = null
    }
}
