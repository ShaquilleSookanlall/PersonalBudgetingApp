package com.example.personalbudgetingapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.personalbudgetingapp.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: AppDatabase

    private val presetCategories = listOf(
        "Food", "Transport", "Utilities", "Health", "Education",
        "Entertainment", "Clothing", "Personal Care", "Miscellaneous"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        db = AppDatabase.getDatabase(this)
        ensureDefaultCategories()

        auth = FirebaseAuth.getInstance()

        // ✅ Show user email dynamically
        val userEmail = auth.currentUser?.email ?: "Guest"
        binding.tvUserEmail.text = "Welcome, $userEmail"

        // ✅ Load and show user's budget goal
        CoroutineScope(Dispatchers.IO).launch {
            val goal = db.appDao().getBudgetGoal()
            goal?.let {
                val display = "Min: R%.2f, Max: R%.2f".format(it.minGoal, it.maxGoal)
                runOnUiThread {
                    binding.tvBudgetAmount.text = display
                }
            }
        }

        // ✅ Card navigation
        binding.cardSetBudgetGoals.setOnClickListener {
            loadFragment(SetBudgetGoalsFragment())
        }

        binding.cardViewEntries.setOnClickListener {
            loadFragment(ViewEntriesFragment())
        }

        binding.cardViewCategoryTotals.setOnClickListener {
            loadFragment(ViewCategoryTotalsFragment())
        }

        // ✅ FAB for creating new entries
        binding.fabCreateEntry.setOnClickListener {
            loadFragment(CreateEntryFragment())
        }

        // ✅ Bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_month -> {
                    binding.fragmentContainer.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
                    binding.tvUserEmail.visibility = View.VISIBLE
                    binding.fabCreateEntry.visibility = View.VISIBLE
                    true
                }
                R.id.nav_categories -> {
                    loadFragment(ViewCategoriesFragment())
                    true
                }
                R.id.nav_dashboard -> {
                    showComingSoon("Dashboard")
//                  loadFragment(DashboardFragment()) // for Part 3
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        binding.scrollView.visibility = View.GONE
        binding.tvUserEmail.visibility = View.GONE
        binding.fabCreateEntry.visibility = View.GONE

        binding.fragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun ensureDefaultCategories() {
        CoroutineScope(Dispatchers.IO).launch {
            val existingCategories = db.appDao().getAllCategories()
            val existingNames = existingCategories.map { it.name }

            for (categoryName in presetCategories) {
                if (existingNames.contains(categoryName)) continue

                val isDeleted = db.appDao().isCategoryDeletedByName(categoryName)
                if (!isDeleted) {
                    db.appDao().insertCategory(Category(name = categoryName))
                }
            }
        }
    }

    private fun showComingSoon(feature: String) {
        Toast.makeText(this, "$feature feature coming in Part 3!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "DashboardActivity"
    }
}