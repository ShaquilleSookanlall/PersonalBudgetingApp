package com.example.personalbudgetingapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.personalbudgetingapp.databinding.FragmentSetBudgetGoalsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetBudgetGoalsFragment : Fragment() {

    private var _binding: FragmentSetBudgetGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentSetBudgetGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")

        db = AppDatabase.getDatabase(requireContext())

        // Load existing budget goals
        CoroutineScope(Dispatchers.IO).launch {
            val budgetGoal = db.appDao().getBudgetGoal()
            withContext(Dispatchers.Main) {
                if (budgetGoal != null) {
                    Log.d(TAG, "Loaded budget goals: minGoal=${budgetGoal.minGoal}, maxGoal=${budgetGoal.maxGoal}")
                    binding.etMinGoal.setText(budgetGoal.minGoal.toString())
                    binding.etMaxGoal.setText(budgetGoal.maxGoal.toString())
                } else {
                    Log.d(TAG, "No budget goals found")
                }
            }
        }

        binding.btnSaveGoals.setOnClickListener {
            Log.d(TAG, "Save Goals button clicked")
            val minGoal = binding.etMinGoal.text.toString().toDoubleOrNull()
            val maxGoal = binding.etMaxGoal.text.toString().toDoubleOrNull()

            if (minGoal == null || maxGoal == null) {
                Log.d(TAG, "Invalid minGoal or maxGoal: minGoal=$minGoal, maxGoal=$maxGoal")
                Toast.makeText(context, "Please enter valid numbers for goals", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minGoal <= 0 || maxGoal <= 0) {
                Log.d(TAG, "Goals must be positive: minGoal=$minGoal, maxGoal=$maxGoal")
                Toast.makeText(context, "Goals must be positive numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minGoal >= maxGoal) {
                Log.d(TAG, "minGoal is greater than or equal to maxGoal: minGoal=$minGoal, maxGoal=$maxGoal")
                Toast.makeText(context, "Maximum goal must be greater than minimum goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Saving budget goals: minGoal=$minGoal, maxGoal=$maxGoal")
                    db.appDao().setBudgetGoal(minGoal, maxGoal)
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Budget goals saved successfully")
                        Toast.makeText(context, "Budget goals saved: Min: R$minGoal, Max: R$maxGoal", Toast.LENGTH_SHORT).show()
                    }
                        // No need to clear fields since we're displaying the current goals

                } catch (e: Exception) {
                    Log.e(TAG, "Error saving budget goals: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error saving budget goals: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
        _binding = null
    }

    companion object {
        private const val TAG = "SetBudgetGoalsFragment"
    }
}