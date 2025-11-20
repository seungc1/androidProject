package com.example.androidproject.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.androidproject.data.local.TypeConverters as AppTypeConverters
import com.example.androidproject.data.local.dao.* // 👈 [수정] Wildcard import
import com.example.androidproject.data.local.entity.* // 👈 [수정] Wildcard import

@Database(
    entities = [
        UserEntity::class,
        ExerciseEntity::class,
        RehabSessionEntity::class,
        DietSessionEntity::class,
        InjuryEntity::class,
        DietEntity::class,
        ScheduledWorkoutEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun rehabSessionDao(): RehabSessionDao
    abstract fun dietSessionDao(): DietSessionDao
    abstract fun injuryDao(): InjuryDao
    abstract fun dietDao(): DietDao
    abstract fun scheduledWorkoutDao(): ScheduledWorkoutDao
}