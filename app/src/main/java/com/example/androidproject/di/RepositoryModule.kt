package com.example.androidproject.di

// (Data Layer)
import com.example.androidproject.data.repository.DietSessionRepositoryImpl
import com.example.androidproject.data.repository.RehabRepositoryImpl
import com.example.androidproject.data.repository.RehabSessionRepositoryImpl
import com.example.androidproject.data.repository.UserRepositoryImpl
import com.example.androidproject.data.repository.InjuryRepositoryImpl     // 👈 [추가]
import com.example.androidproject.data.repository.DietRepositoryImpl      // 👈 [추가]
import com.example.androidproject.data.repository.WorkoutRoutineRepositoryImpl // 👈 [추가]

// (Domain Layer)
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository
import com.example.androidproject.domain.repository.InjuryRepository     // 👈 [추가]
import com.example.androidproject.domain.repository.DietRepository      // 👈 [추가]
import com.example.androidproject.domain.repository.WorkoutRoutineRepository // 👈 [추가]

// (Dagger/Hilt)
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRehabRepository(
        rehabRepositoryImpl: RehabRepositoryImpl
    ): RehabRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindRehabSessionRepository(
        rehabSessionRepositoryImpl: RehabSessionRepositoryImpl
    ): RehabSessionRepository

    @Binds
    @Singleton
    abstract fun bindDietSessionRepository(
        dietSessionRepositoryImpl: DietSessionRepositoryImpl
    ): DietSessionRepository

    // 🚨 [추가] 3개의 새 Repository 바인딩

    @Binds
    @Singleton
    abstract fun bindInjuryRepository(
        injuryRepositoryImpl: InjuryRepositoryImpl
    ): InjuryRepository

    @Binds
    @Singleton
    abstract fun bindDietRepository(
        dietRepositoryImpl: DietRepositoryImpl
    ): DietRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRoutineRepository(
        workoutRoutineRepositoryImpl: WorkoutRoutineRepositoryImpl
    ): WorkoutRoutineRepository
}