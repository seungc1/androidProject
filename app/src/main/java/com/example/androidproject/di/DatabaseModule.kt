package com.example.androidproject.di

import android.content.Context
import androidx.room.Room
import com.example.androidproject.data.local.AppDatabase
import com.example.androidproject.data.local.dao.* // 👈 [수정] Wildcard import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rehab_ai_db"
        )
            // (DB 버전이 2->3으로 올라갔으므로, 기존 DB는 삭제하고 새로 만듭니다)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    @Provides
    @Singleton
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }
    @Provides
    @Singleton
    fun provideRehabSessionDao(database: AppDatabase): RehabSessionDao {
        return database.rehabSessionDao()
    }
    @Provides
    @Singleton
    fun provideDietSessionDao(database: AppDatabase): DietSessionDao {
        return database.dietSessionDao()
    }

    @Provides
    @Singleton
    fun provideInjuryDao(database: AppDatabase): InjuryDao {
        return database.injuryDao()
    }

    @Provides
    @Singleton
    fun provideDietDao(database: AppDatabase): DietDao {
        return database.dietDao()
    }
}