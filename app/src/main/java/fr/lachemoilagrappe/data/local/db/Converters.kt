package fr.lachemoilagrappe.data.local.db

import androidx.room.TypeConverter
import fr.lachemoilagrappe.domain.model.CallDecision
import fr.lachemoilagrappe.domain.model.ListType
import fr.lachemoilagrappe.domain.model.SmsStatus

class Converters {

    @TypeConverter
    fun fromCallDecision(value: CallDecision): String = value.name

    @TypeConverter
    fun toCallDecision(value: String): CallDecision = CallDecision.valueOf(value)

    @TypeConverter
    fun fromListType(value: ListType): String = value.name

    @TypeConverter
    fun toListType(value: String): ListType = ListType.valueOf(value)

    @TypeConverter
    fun fromSmsStatus(value: SmsStatus): String = value.name

    @TypeConverter
    fun toSmsStatus(value: String): SmsStatus = SmsStatus.valueOf(value)
}
