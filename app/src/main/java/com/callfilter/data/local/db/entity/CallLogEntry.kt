package com.callfilter.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.callfilter.domain.model.CallDecision

@Entity(
    tableName = "call_log",
    indices = [
        Index(value = ["normalizedNumber"]),
        Index(value = ["timestamp"])
    ]
)
data class CallLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val timestamp: Long,
    val decision: CallDecision,
    val reason: String,
    val contactName: String? = null,
    val spamTag: String? = null,
    val spamScore: Int? = null
)
