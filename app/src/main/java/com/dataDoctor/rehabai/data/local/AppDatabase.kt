package com.dataDoctor.rehabai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dataDoctor.rehabai.data.local.TypeConverters as AppTypeConverters
import com.dataDoctor.rehabai.data.local.dao.*
import com.dataDoctor.rehabai.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        ExerciseEntity::class,
        RehabSessionEntity::class,
        DietSessionEntity::class,
        InjuryEntity::class,
        DietEntity::class,
        ScheduledWorkoutEntity::class,
        ScheduledDietEntity::class
    ],
    version = 8,
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
    abstract fun scheduledDietDao(): ScheduledDietDao
}