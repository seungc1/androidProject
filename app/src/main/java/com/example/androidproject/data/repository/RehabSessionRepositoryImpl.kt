package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.repository.RehabSessionRepository
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
class RehabSessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource // (★ 추가 주입)
) : RehabSessionRepository {

    override suspend fun addRehabSession(session: RehabSession): Flow<Unit> {
        // 1. Firebase 저장
        firebaseDataSource.addRehabSession(session)
        // 2. Local 저장
        localDataSource.addRehabSession(session.toEntity())
        return flowOf(Unit)
    }

    override suspend fun getRehabHistory(userId: String): Flow<List<RehabSession>> {
        // 1. 로컬 데이터 구독
        val localData = localDataSource.getRehabHistory(userId).map { entityList ->
            entityList.map { it.toDomain() }
        }

        // 2. 서버 데이터 동기화
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteSessions = firebaseDataSource.getRehabHistory(userId)
                remoteSessions.forEach { session ->
                    localDataSource.addRehabSession(session.toEntity())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return localData
    }

    override suspend fun getRehabSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSession>> {
        // (기간 조회는 보통 분석용이므로 로컬 데이터만 사용하거나, 필요 시 별도 동기화 로직 추가)
        return localDataSource.getRehabSessionsBetween(userId, startDate, endDate).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }
}