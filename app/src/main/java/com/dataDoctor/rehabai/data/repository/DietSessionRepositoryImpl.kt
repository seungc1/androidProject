package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.local.datasource.LocalDataSource
import com.dataDoctor.rehabai.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.dataDoctor.rehabai.data.mapper.toDomain
import com.dataDoctor.rehabai.data.mapper.toEntity
import com.dataDoctor.rehabai.domain.model.DietSession
import com.dataDoctor.rehabai.domain.repository.DietSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietSessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource // (★ 추가 주입)
) : DietSessionRepository {

    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        android.util.Log.d("DIET_REPO", "addDietSession called: ${session.id}, foodName=${session.foodName}")
        
        try {
            // 1. Firebase 저장
            android.util.Log.d("DIET_REPO", "Saving to Firebase...")
            firebaseDataSource.addDietSession(session)
            android.util.Log.d("DIET_REPO", "Firebase save complete")
            
            // 2. Local 저장
            android.util.Log.d("DIET_REPO", "Saving to local DB...")
            localDataSource.addDietSession(session.toEntity())
            android.util.Log.d("DIET_REPO", "Local DB save complete")
        } catch (e: Exception) {
            android.util.Log.e("DIET_REPO", "Error in addDietSession: ${e.message}", e)
            throw e
        }
        
        return flowOf(Unit)
    }

    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        // 1. 로컬 데이터 구독
        val localData = localDataSource.getDietHistory(userId).map { entityList ->
            entityList.map { it.toDomain() }
        }

        // 2. 서버 데이터 동기화
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteSessions = firebaseDataSource.getDietHistory(userId)
                remoteSessions.forEach { session ->
                    localDataSource.addDietSession(session.toEntity())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return localData
    }

    override suspend fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSession>> {
        return localDataSource.getDietSessionsBetween(userId, startDate, endDate).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }
}