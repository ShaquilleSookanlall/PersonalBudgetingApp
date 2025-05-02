package com.example.personalbudgetingapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.personalbudgetingapp.databinding.FragmentViewEntriesBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ViewEntriesFragment : Fragment() {

    private var _binding: FragmentViewEntriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

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
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        binding.rvEntries.layoutManager = LinearLayoutManager(context)

        // Load all entries initially
        loadEntries("0000-01-01", "9999-12-31")

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

        binding.btnFilterEntries.setOnClickListener {
            val start = binding.etStartDate.text.toString().ifEmpty { "0000-01-01" }
            val end = binding.etEndDate.text.toString().ifEmpty { "9999-12-31" }
            loadEntries(start, end)
        }
    }

    private fun loadEntries(startDate: String, endDate: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val entries = db.appDao().getEntriesInPeriod(startDate, endDate)
                .filter { it.userId == userId }

            val categories = db.appDao().getAllCategories()
            withContext(Dispatchers.Main) {
                if (entries.isEmpty()) {
                    Toast.makeText(requireContext(), "No entries found", Toast.LENGTH_SHORT).show()
                }
                binding.rvEntries.adapter = EntryAdapter(entries, categories) { selectedEntry ->
                    // Optional: Handle click for preview/edit
                    Toast.makeText(context, "Clicked: ${selectedEntry.description}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
