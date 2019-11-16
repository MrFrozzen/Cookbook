package ru.mrfrozzen.cookbook.presentation.main


import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import kotlinx.android.synthetic.main.fragment_favorite_recipes.*

class FavoriteRecipesFragment : Fragment() {

    private var listener: RecipeFragmentListener? = null
    private var adapter: RecipeAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list_favorites.setOnItemClickListener { _, _, position, _ ->
            val r = adapter?.getItem(position)?.recipe
            if (r != null) listener?.onRecipeSelected(r.id)
        }
        registerForContextMenu(list_favorites)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val menuInflater = activity!!.menuInflater
        menuInflater.inflate(R.menu.context_menu_recipe, menu)
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
            val info = item!!.menuInfo as AdapterView.AdapterContextMenuInfo
            val r = adapter?.getItem(info.position)?.recipe
            val c = adapter?.getItem(info.position)?.category
            if (r != null) {
                return when (item.itemId) {
                    R.id.action_edit_recipe -> {
                        listener!!.onRecipeEdit(r.id)
                        true
                    }
                    R.id.action_share -> {
                        listener!!.onRecipeShare(RecipeWithCategory(r, c))
                        true
                    }
                    R.id.action_delete_recipe -> {
                        listener!!.onRecipeDelete(r)
                        true
                    }
                    else -> super.onContextItemSelected(item)
                }
            }
        }
        return false
    }

    private fun subscribeUi() {

        val viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        viewModel.favoriteRecipesWithCategory.observe(viewLifecycleOwner, Observer { data ->
            if (data == null || data.isEmpty()) {
                text_no_favorites.visibility = VISIBLE
            } else {
                text_no_favorites.visibility = GONE
            }
            adapter = RecipeAdapter(requireContext(), data)
            list_favorites.adapter = adapter
        })
    }

    companion object {

        internal fun newInstance(): FavoriteRecipesFragment {
            return FavoriteRecipesFragment()
        }
    }

}
