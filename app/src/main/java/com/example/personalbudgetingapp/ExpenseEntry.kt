package com.example.personalbudgetingapp

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "expense_entries",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"]), Index(value = ["userId"])]
)
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val description: String,
    val categoryId: Int,
    val amount: Double,
    val photoUri: String?,
    val userId: String
) : Parcelable
