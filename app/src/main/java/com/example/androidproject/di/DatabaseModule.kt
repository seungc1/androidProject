package com.example.androidproject.di

import android.content.Context
import androidx.room.Room
import com.example.androidproject.data.local.AppDatabase
import com.example.androidproject.data.local.dao.ExerciseDao
import com.example.androidproject.data.local.dao.UserDao
import com.example.androidproject.data.local.dao.DietSessionDao
import com.example.androidproject.data.local.dao.RehabSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * AppDatabase 인스턴스를 생성하여 제공 (공장)
     * 앱 전체에서 단 하나만 존재 (Singleton)
     */
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "rehab_ai_db" // DB 파일 이름
        ).build()
    }

    /**
     * UserDao를 제공
     * Hilt가 위에서 만든 AppDatabase를 여기에 자동으로 주입해 줌.
     * 이 함수가 반환한 UserDao가 RehabRepositoryImpl의 생성자로 주입됨.
     */
    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    /**
     * ExerciseDao를 제공
     * Hilt가 위에서 만든 AppDatabase를 여기에 자동으로 주입해 줌.
     * 이 함수가 반환한 ExerciseDao가 RehabRepositoryImpl의 생성자로 주입됨.
     */
    @Provides
    @Singleton
    fun provideExerciseDao(database: AppDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    /**
     * RehabSessionDao를 제공
     * Hilt가 위에서 만든 AppDatabase를 여기에 자동으로 주입해 줌.
     */
    @Provides
    @Singleton
    fun provideRehabSessionDao(database: AppDatabase): RehabSessionDao {
        return database.rehabSessionDao()
    }

    /**
     * DietSessionDao를 제공
     * Hilt가 위에서 만든 AppDatabase를 여기에 자동으로 주입해 줌.
     */
    @Provides
    @Singleton
    fun provideDietSessionDao(database: AppDatabase): DietSessionDao {
        return database.dietSessionDao()
    }
}