package com.example.personalbudgetingapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalbudgetingapp.databinding.FragmentViewEntriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import androidx.room.Delete


class ViewEntriesFragment : Fragment() {

    private var _binding: FragmentViewEntriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var entries: List<ExpenseEntry>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewEntriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        binding.rvEntries.layoutManager = LinearLayoutManager(context)

        loadEntries("0000-01-01", "9999-12-31")

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

        binding.btnFilterEntries.setOnClickListener {
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()
            val finalStart = if (startDate.isEmpty()) "0000-01-01" else startDate
            val finalEnd = if (endDate.isEmpty()) "9999-12-31" else endDate
            loadEntries(finalStart, finalEnd)
        }
    }

    private fun loadEntries(start: String, end: String) {
        CoroutineScope(Dispatchers.IO).launch {
            entries = db.appDao().getEntriesInPeriod(start, end)
            val categories = db.appDao().getAllCategories()
            withContext(Dispatchers.Main) {
                if (entries.isEmpty()) {
                    Toast.makeText(context, "No entries found", Toast.LENGTH_SHORT).show()
                }
                binding.rvEntries.adapter = EntryAdapter(entries, categories) { entry ->
                    showEntryDetails(entry, categories)
                }
            }
        }
    }

    private fun showEntryDetails(entry: ExpenseEntry, categories: List<Category>) {
        val category = categories.find { it.id == entry.categoryId }?.name ?: "Unknown"
        val photoMsg = if (!entry.photoUri.isNullOrEmpty()) "\nPhoto: Attached" else "\nPhoto: None"

        val message = """
            Description: ${entry.description}
            Date: ${entry.date}
            Category: $category
            Amount: R%.2f$photoMsg
        """.trimIndent().format(entry.amount)

        AlertDialog.Builder(requireContext())
            .setTitle("Expense Details")
            .setMessage(message)
            .setPositiveButton("Edit") { _, _ ->
                editEntry(entry)
            }
            .setNegativeButton("Delete") { _, _ ->
                confirmDelete(entry)
            }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun confirmDelete(entry: ExpenseEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this entry?")
            .setPositiveButton("Yes") { _, _ ->
                deleteEntry(entry)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteEntry(entry: ExpenseEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            db.appDao().deleteExpenseEntry(entry)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Entry deleted", Toast.LENGTH_SHORT).show()
                loadEntries("0000-01-01", "9999-12-31")
            }
        }
    }

    private fun editEntry(entry: ExpenseEntry) {
        val fragment = EditEntryFragment.newInstance(entry)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}