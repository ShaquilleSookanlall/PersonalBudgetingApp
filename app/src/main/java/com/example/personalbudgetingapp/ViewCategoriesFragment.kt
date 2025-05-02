package com.example.personalbudgetingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalbudgetingapp.databinding.FragmentViewCategoriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ViewCategoriesFragment : Fragment() {

    private var _binding: FragmentViewCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        binding.rvCategories.layoutManager = LinearLayoutManager(context)
        loadCategories()

        binding.btnOpenCreateCategory.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(android.R.id.content, CreateCategoryFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.appDao().getAllCategories()
            activity?.runOnUiThread {
                binding.rvCategories.adapter = CategoryListAdapter(categories, db) {
                    loadCategories()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}