package com.example.personalbudgetingapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey val id: Int = 1, // Fixed ID since we only store one budget goal range
    val minGoal: Double,
    val maxGoal: Double
)