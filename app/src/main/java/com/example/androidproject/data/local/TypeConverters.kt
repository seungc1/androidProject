package com.example.androidproject.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room DB가 Date 타입을 Long(Timestamp)으로 변환하여 저장할 수 있도록 돕는
 * '타입 변환기' 클래스입니다.
 */
class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}