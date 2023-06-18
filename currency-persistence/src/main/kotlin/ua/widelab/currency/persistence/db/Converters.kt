package ua.widelab.currency.persistence.db

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.LocalDate

internal class Converters {
    @TypeConverter
    fun fromBigDecimalToString(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun fromStringToBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun fromLocalDateToLong(value: LocalDate?): Long? {
        return value?.toEpochDay()
    }

    @TypeConverter
    fun fromLongToLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

}