package com.callfilter.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spam_db",
    indices = [
        Index(value = ["tag"]),
        Index(value = ["score"])
    ]
)
data class SpamEntry(
    @PrimaryKey
    val normalizedNumber: String,
    val tag: String,
    val score: Int,
    val source: String,
    val lastSeen: Long,
    val updatedAt: Long
)
