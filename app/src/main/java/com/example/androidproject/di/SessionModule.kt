package com.example.androidproject.di

import android.content.Context
import android.content.SharedPreferences
import com.example.androidproject.data.local.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [새 파일 2/2]
 * SharedPreferences와 SessionManager를 Hilt에 '주입'하는 '모듈'
 */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    private const val PREFS_NAME = "rehab_ai_prefs"

    /**
     * SharedPreferences '객체'를 '생성'하여 '제공' (공장)
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * SessionManager '객체'를 '생성'하여 '제공' (공장)
     * (Hilt가 '자동'으로 '위' 'SharedPreferences'를 '여기에' '주입'함)
     */
    @Provides
    @Singleton
    fun provideSessionManager(prefs: SharedPreferences): SessionManager {
        return SessionManager(prefs)
    }
}