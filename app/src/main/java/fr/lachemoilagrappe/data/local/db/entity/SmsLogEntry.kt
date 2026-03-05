package fr.lachemoilagrappe.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.lachemoilagrappe.domain.model.SmsStatus

@Entity(
    tableName = "sms_log",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["timestamp"])
    ]
)
data class SmsLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val normalizedNumber: String,
    val timestamp: Long,
    val status: SmsStatus,
    val templateUsed: String
)
