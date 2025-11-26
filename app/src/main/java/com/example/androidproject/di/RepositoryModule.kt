package com.example.androidproject.di

// (Data Layer)
import com.example.androidproject.data.repository.DietSessionRepositoryImpl
import com.example.androidproject.data.repository.RehabRepositoryImpl
import com.example.androidproject.data.repository.RehabSessionRepositoryImpl
import com.example.androidproject.data.repository.UserRepositoryImpl
import com.example.androidproject.data.repository.InjuryRepositoryImpl
import com.example.androidproject.data.repository.DietRepositoryImpl
import com.example.androidproject.data.repository.WorkoutRoutineRepositoryImpl
// import com.example.androidproject.data.repository.AIApiRepositoryImpl // 👈 이 import는 더 이상 필요하지 않습니다.

// (Domain Layer)
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository
import com.example.androidproject.domain.repository.InjuryRepository
import com.example.androidproject.domain.repository.DietRepository
import com.example.androidproject.domain.repository.WorkoutRoutineRepository
// import com.example.androidproject.domain.repository.AIApiRepository // 👈 이 import는 bindAIApiRepository가 없으므로 필요하지 않습니다.

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

    // 🚨🚨🚨 이 바인딩은 'src/debug'와 'src/release'의 AIApiModule에 의해 중복되므로 삭제합니다.
    /*
    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        aiApiRepositoryImpl: AIApiRepositoryImpl
    ): AIApiRepository
    */
}