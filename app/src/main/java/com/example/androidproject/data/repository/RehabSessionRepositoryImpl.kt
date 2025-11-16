package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RehabSessionRepositoryImpl @Inject constructor() : RehabSessionRepository {

    private val _rehabSessions = MutableStateFlow<List<RehabSession>>(emptyList())

    override suspend fun addRehabSession(session: RehabSession): Flow<Unit> {
        val newSession = session.copy(id = session.id.ifEmpty { UUID.randomUUID().toString() })
        _rehabSessions.value = _rehabSessions.value + newSession
        return flowOf(Unit)
    }

    override suspend fun getRehabHistory(userId: String): Flow<List<RehabSession>> {
        // StateFlow를 직접 map하여 필터링
        return _rehabSessions.map { sessions -> sessions.filter { it.userId == userId } }
    }

    override suspend fun getRehabSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSession>> {
        // (현재는 더미 데이터이므로 .filter를 사용합니다. 실제 DB에서는 Query를 사용합니다.)
        return _rehabSessions.map { sessions ->
            sessions.filter {
                // 사용자가 일치하고, 날짜가 startDate 이후(before가 아님)이고 endDate 이전(after가 아님)인 경우
                it.userId == userId && !it.dateTime.before(startDate) && !it.dateTime.after(endDate)
            }
        }
    }
}