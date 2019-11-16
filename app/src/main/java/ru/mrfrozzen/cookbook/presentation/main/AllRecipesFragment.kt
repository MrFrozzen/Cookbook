package ru.mrfrozzen.cookbook.presentation.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ru.mrfrozzen.cookbook.R
import ru.mrfrozzen.cookbook.data.RecipeWithCategory
import ru.mrfrozzen.cookbook.utilities.FILTER_RECIPE_ACTION
import ru.mrfrozzen.cookbook.utilities.INTENT_EXTRA_NAME_FILTER
import ru.mrfrozzen.cookbook.utilities.InjectorUtils
import kotlinx.android.synthetic.main.fragment_all_recipes.*

class AllRecipesFragment : Fragment() {

    private var adapter: RecipeAdapter? = null
    private var listener: RecipeFragmentListener? = null

    private val filterReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val filterString = intent.getStringExtra(INTENT_EXTRA_NAME_FILTER)
            adapter?.filter?.filter(filterString)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_recipes, container, false)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val menuInflater = activity?.menuInflater
        menuInflater?.inflate(R.menu.context_menu_recipe, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list_recipes_all.setOnItemClickListener { _, _, position, _ ->
            val r = adapter?.getItem(position)?.recipe
            if (r != null) listener?.onRecipeSelected(r.id)
        }

        registerForContextMenu(list_recipes_all)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RecipeFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(requireContext().toString() + " must implement RecipeFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        val factory = InjectorUtils.provideMainViewModelFactory(requireContext())
        val viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)
        viewModel.recipesWithCategory.observe(viewLifecycleOwner, Observer { recipesWithCategory ->
            if (recipesWithCategory == null || recipesWithCategory.isEmpty()) {
                text_no_recipes.visibility = VISIBLE
            } else {
                text_no_recipes.visibility = GONE
            }
            adapter = RecipeAdapter(requireContext(), recipesWithCategory)
            list_recipes_all.adapter = adapter
        })
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(filterReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(filterReceiver)
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter(FILTER_RECIPE_ACTION)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(filterReceiver, filter)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (userVisibleHint) {
            val info = item?.menuInfo as AdapterView.AdapterContextMenuInfo
            val r = adapter?.getItem(info.position)?.recipe
            val c = adapter?.getItem(info.position)?.category
            if (r != null) {
                when (item.itemId) {
                    R.id.action_edit_recipe -> {
                        listener?.onRecipeEdit(r.id)
                        return true
                    }
                    R.id.action_share -> {
                        listener?.onRecipeShare(RecipeWithCategory(r, c))
                        return true
                    }
                    R.id.action_delete_recipe -> {
                        listener?.onRecipeDelete(r)
                        return true
                    }
                    else -> return super.onContextItemSelected(item)
                }
            }
        }
        return false
    }

    companion object {

        internal fun newInstance(): AllRecipesFragment {
            return AllRecipesFragment()
        }
    }
}
