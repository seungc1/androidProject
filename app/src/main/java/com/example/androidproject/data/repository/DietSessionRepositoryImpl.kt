package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource // ✅ [추가]
import com.example.androidproject.data.mapper.toDomain // ✅ [추가] (3-1단계에서 만듦)
import com.example.androidproject.data.mapper.toEntity // ✅ [추가] (3-1단계에서 만듦)
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.repository.DietSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // ✅ [추가]
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
// ✅ [수정] Hilt가 LocalDataSource를 주입하도록 생성자 변경
class DietSessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : DietSessionRepository {

    // ❌ --- 'MutableStateFlow' (더미 데이터) 관련 코드 '전부 삭제' ---
    // private val _dietSessions = ...
    // ❌ ----------------------------------------------------

    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        // ✅ [수정] Domain 모델(Session)을 Entity로 '번역'하여 LocalDataSource에 전달
        localDataSource.addDietSession(session.toEntity())
        return flowOf(Unit) // 성공 반환
    }

    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        // ✅ [수정] LocalDataSource에서 Entity 리스트를 받아 Domain 리스트로 '번역'하여 반환
        return localDataSource.getDietHistory(userId).map { entityList ->
            entityList.map { it.toDomain() } // it == DietSessionEntity
        }
    }

    override suspend fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSession>> {
        // ✅ [수정] LocalDataSource에서 기간별 Entity 리스트를 받아 Domain 리스트로 '번역'
        return localDataSource.getDietSessionsBetween(userId, startDate, endDate).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }
}