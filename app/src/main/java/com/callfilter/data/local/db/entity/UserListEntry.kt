package com.callfilter.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.callfilter.domain.model.ListType

@Entity(
    tableName = "user_list",
    indices = [
        Index(value = ["listType"])
    ]
)
data class UserListEntry(
    @PrimaryKey
    val normalizedNumber: String,
    val listType: ListType,
    val label: String? = null,
    val addedAt: Long
)
