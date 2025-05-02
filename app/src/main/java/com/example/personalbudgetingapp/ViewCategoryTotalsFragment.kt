package com.example.personalbudgetingapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalbudgetingapp.databinding.FragmentViewCategoryTotalsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ViewCategoryTotalsFragment : Fragment() {

    private var _binding: FragmentViewCategoryTotalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewCategoryTotalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        binding.rvCategoryTotals.layoutManager = LinearLayoutManager(context)

        // Load all totals on start
        loadCategoryTotals("0000-01-01", "9999-12-31")

        // Date pickers
        binding.etStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.etStartDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                binding.etEndDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnFilterTotals.setOnClickListener {
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()
            val finalStart = if (startDate.isEmpty()) "0000-01-01" else startDate
            val finalEnd = if (endDate.isEmpty()) "9999-12-31" else endDate
            loadCategoryTotals(finalStart, finalEnd)
        }
    }

    private fun loadCategoryTotals(start: String, end: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.appDao().getAllCategories()
            val totals = mutableListOf<Pair<Category, Double>>()
            for (category in categories) {
                val total = db.appDao().getTotalForCategory(category.id, start, end) ?: 0.0
                totals.add(category to total)
            }
            activity?.runOnUiThread {
                binding.rvCategoryTotals.adapter = CategoryTotalAdapter(totals)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
