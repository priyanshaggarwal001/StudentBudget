package com.studentbudget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val nameKey: String,
    val icon: String,
    val label: String,
    val type: String // "expense" or "income"
)
