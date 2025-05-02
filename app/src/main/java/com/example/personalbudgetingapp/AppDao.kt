package com.example.personalbudgetingapp

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert

@Dao
interface AppDao {

    @Insert
    suspend fun insertExpenseEntry(entry: ExpenseEntry)

    @Delete
    suspend fun deleteExpenseEntry(entry: ExpenseEntry)

    @Update
    suspend fun updateExpenseEntry(entry: ExpenseEntry) //

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM expense_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getEntriesInPeriod(startDate: String, endDate: String): List<ExpenseEntry> {
        Log.d("AppDao", "getEntriesInPeriod called with startDate=$startDate, endDate=$endDate")
        return try {
            val entries = getEntriesInPeriodInternal(startDate, endDate)
            Log.d("AppDao", "Found ${entries.size} entries")
            entries
        } catch (e: Exception) {
            Log.e("AppDao", "Error querying entries: ${e.message}", e)
            emptyList()
        }
    }

    @Query("SELECT * FROM expense_entries WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getEntriesInPeriodInternal(startDate: String, endDate: String): List<ExpenseEntry>

    @Query("SELECT * FROM categories WHERE deleted = 0")
    suspend fun getAllCategories(): List<Category>

    @Query("UPDATE categories SET deleted = 1 WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)

    @Query("SELECT SUM(amount) FROM expense_entries WHERE categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalForCategory(categoryId: Int, startDate: String, endDate: String): Double?

    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name AND deleted = 1)")
    suspend fun isCategoryDeletedByName(name: String): Boolean

    @Upsert
    suspend fun setBudgetGoal(budgetGoal: BudgetGoal)

    suspend fun setBudgetGoal(minGoal: Double, maxGoal: Double) {
        val budgetGoal = BudgetGoal(id = 1, minGoal = minGoal, maxGoal = maxGoal)
        setBudgetGoal(budgetGoal)
    }

    @Query("SELECT * FROM budget_goals WHERE id = 1")
    suspend fun getBudgetGoal(): BudgetGoal?
}