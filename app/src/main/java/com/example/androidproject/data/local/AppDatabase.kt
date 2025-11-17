package com.example.androidproject.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // ✅ [추가] 타입 변환기 import
import com.example.androidproject.data.local.TypeConverters as AppTypeConverters
import com.example.androidproject.data.local.dao.DietSessionDao // ✅ [추가]
import com.example.androidproject.data.local.dao.ExerciseDao
import com.example.androidproject.data.local.dao.RehabSessionDao // ✅ [추가]
import com.example.androidproject.data.local.dao.UserDao
import com.example.androidproject.data.local.entity.DietSessionEntity // ✅ [추가]
import com.example.androidproject.data.local.entity.ExerciseEntity
import com.example.androidproject.data.local.entity.RehabSessionEntity // ✅ [추가]
import com.example.androidproject.data.local.entity.UserEntity

@Database(
    // ✅ [수정] entities 배열에 새로 만든 Entity 2개 추가
    entities = [
        UserEntity::class,
        ExerciseEntity::class,
        RehabSessionEntity::class,
        DietSessionEntity::class
    ],
    version = 2, // (참고: DB 구조가 바뀌면 'version'을 올려야 하지만, 지금은 1로 유지)
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class) // ✅ [추가] 1단계에서 만든 TypeConverters 등록
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao

    // ✅ [추가] 새로 만든 DAO 2개를 위한 추상 함수 추가
    abstract fun rehabSessionDao(): RehabSessionDao
    abstract fun dietSessionDao(): DietSessionDao
}