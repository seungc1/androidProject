package com.dataDoctor.rehabai.di

// (Data Layer)
import com.dataDoctor.rehabai.data.repository.DietSessionRepositoryImpl
import com.dataDoctor.rehabai.data.repository.RehabRepositoryImpl
import com.dataDoctor.rehabai.data.repository.RehabSessionRepositoryImpl
import com.dataDoctor.rehabai.data.repository.UserRepositoryImpl
import com.dataDoctor.rehabai.data.repository.InjuryRepositoryImpl
import com.dataDoctor.rehabai.data.repository.DietRepositoryImpl
import com.dataDoctor.rehabai.data.repository.WorkoutRoutineRepositoryImpl
// import com.example.androidproject.data.repository.AIApiRepositoryImpl // 👈 이 import는 더 이상 필요하지 않습니다.

// (Domain Layer)
import com.dataDoctor.rehabai.domain.repository.DietSessionRepository
import com.dataDoctor.rehabai.domain.repository.RehabRepository
import com.dataDoctor.rehabai.domain.repository.RehabSessionRepository
import com.dataDoctor.rehabai.domain.repository.UserRepository
import com.dataDoctor.rehabai.domain.repository.InjuryRepository
import com.dataDoctor.rehabai.domain.repository.DietRepository
import com.dataDoctor.rehabai.domain.repository.WorkoutRoutineRepository
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