package com.dataDoctor.rehabai.data.local

import androidx.room.TypeConverter
import com.dataDoctor.rehabai.domain.model.ExerciseRecommendation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    private val gson = Gson() // (Gson 인스턴스 생성)

    @TypeConverter
    fun fromExerciseRecommendationList(value: List<ExerciseRecommendation>?): String? {
        if (value == null) return null
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExerciseRecommendationList(value: String?): List<ExerciseRecommendation>? {
        if (value == null) return null
        // (JSON 문자열을 List<ExerciseRecommendation> 타입으로 변환)
        val listType = object : TypeToken<List<ExerciseRecommendation>>() {}.type
        return gson.fromJson(value, listType)
    }
}