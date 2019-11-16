package ru.mrfrozzen.cookbook.presentation.main

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import ru.mrfrozzen.cookbook.data.db.entity.Category
import ru.mrfrozzen.cookbook.data.db.entity.Recipe
import ru.mrfrozzen.cookbook.utilities.InjectorUtils
import kotlinx.android.synthetic.main.fragment_categories.*
import java.util.*

class CategoriesFragment : Fragment() {

    private var adapter: CategoryAdapter? = null
    private var listener: RecipeFragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val menuInflater = activity?.menuInflater

        val info = menuInfo as ExpandableListView.ExpandableListContextMenuInfo
        val type = ExpandableListView.getPackedPositionType(info.packedPosition)

        // Context menu for groups (categories)
        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            menuInflater?.inflate(R.menu.context_menu_categories, menu)
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            // Context menu for children (recipes)
            menuInflater?.inflate(R.menu.context_menu_recipe, menu)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list_categories.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val recipe = adapter?.getChild(groupPosition, childPosition)
            if (recipe != null) {
                listener?.onRecipeSelected(recipe.id)
            }
            false
        }

        registerForContextMenu(list_categories)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RecipeFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement RecipeFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        subscribeUi()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (userVisibleHint) {
            val info = item!!.menuInfo as ExpandableListView.ExpandableListContextMenuInfo
            val a = adapter
            if (a != null) {
                when (item.itemId) {
                    R.id.action_edit_category -> {
                        listener?.onCategoryEdit(a.getGroup(ExpandableListView
                                .getPackedPositionGroup(info.packedPosition)))
                        return true
                    }
                    R.id.action_delete_category -> {
                        listener?.onCategoryDelete(a.getGroup(ExpandableListView
                                .getPackedPositionGroup(info.packedPosition)))
                        return true
                    }
                    R.id.action_edit_recipe -> {
                        listener?.onRecipeEdit(a.getChild(ExpandableListView
                                .getPackedPositionGroup(info.packedPosition), ExpandableListView
                                .getPackedPositionChild(info.packedPosition)).id)
                        return true
                    }
                    R.id.action_share -> {
                        val recipe = a.getChild(ExpandableListView
                                .getPackedPositionGroup(info.packedPosition), ExpandableListView
                                .getPackedPositionChild(info.packedPosition))
                        val category = a.getGroup(ExpandableListView
                                .getPackedPositionGroup(info.packedPosition))
                        listener?.onRecipeShare(RecipeWithCategory(recipe, category))
                        return true
                    }
                    R.id.action_delete_recipe -> {
                        listener?.onRecipeDelete(a.getChild(ExpandableListView
                                .getPackedPositionGroup(info.packedPosition), ExpandableListView
                                .getPackedPositionChild(info.packedPosition)))
                        return true
                    }
                    else -> return super.onContextItemSelected(item)
                }
            }
        }
        return false
    }

    private fun subscribeUi() {
        val factory = InjectorUtils.provideMainViewModelFactory(requireContext())
        val viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        viewModel.categoriesWithRecipes.observe(viewLifecycleOwner, Observer { data ->
            if (data == null || data.isEmpty()) {
                text_no_categories.visibility = View.VISIBLE
            } else {
                text_no_categories.visibility = View.GONE
            }
            if (data != null) {
                val categories = ArrayList<Category>()
                val categoryMap = HashMap<Category, List<Recipe>>()
                for (cwr in data) {
                    val c = cwr.category
                    val r = cwr.recipes
                    if (c != null && r != null) {
                        categories.add(c)
                        categoryMap[c] = r
                    }
                }
                adapter = CategoryAdapter(requireContext(), categories, categoryMap)
                list_categories.setAdapter(adapter)
            }
        })
    }

    companion object {

        internal fun newInstance(): CategoriesFragment {
            return CategoriesFragment()
        }
    }
}
