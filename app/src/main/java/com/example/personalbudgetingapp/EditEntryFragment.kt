package com.example.personalbudgetingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.personalbudgetingapp.databinding.FragmentEditEntryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditEntryFragment : Fragment() {

    private var _binding: FragmentEditEntryBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private var entry: ExpenseEntry? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        entry = arguments?.getParcelable("entry")

        CoroutineScope(Dispatchers.IO).launch {
            val categories = db.appDao().getAllCategories()
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter

                entry?.let { e ->
                    binding.etDate.setText(e.date)
                    binding.etDescription.setText(e.description)
                    binding.etAmount.setText(e.amount.toString())

                    val categoryName = categories.find { it.id == e.categoryId }?.name
                    val position = categories.indexOfFirst { it.name == categoryName }
                    if (position >= 0) binding.spinnerCategory.setSelection(position)
                }
            }
        }

        binding.btnUpdate.setOnClickListener {
            val updatedEntry = entry?.copy(
                date = binding.etDate.text.toString(),
                description = binding.etDescription.text.toString(),
                amount = binding.etAmount.text.toString().toDoubleOrNull() ?: 0.0,
                categoryId = binding.spinnerCategory.selectedItemPosition + 1 // Assumes 1-based index
            )

            if (updatedEntry != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    db.appDao().updateExpenseEntry(updatedEntry)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Entry updated", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(entry: ExpenseEntry): EditEntryFragment {
            val fragment = EditEntryFragment()
            val args = Bundle()
            args.putParcelable("entry", entry)
            fragment.arguments = args
            return fragment
        }
    }
}
