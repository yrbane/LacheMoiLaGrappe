package fr.lachemoilagrappe.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phishing_sms",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["timestamp"])
    ]
)
data class PhishingSmsEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long,
    val body: String,
    val matchedKeyword: String? = null,
    val isRead: Boolean = false
)
