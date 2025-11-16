// app/src/test/java/com/example/androidproject/data/repository/FakeRehabSessionRepository.kt
package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class FakeRehabSessionRepository : RehabSessionRepository {
    private val _rehabSessions = MutableStateFlow<List<RehabSession>>(emptyList())

    // 테스트 시 쉽게 데이터를 추가할 수 있도록 함수 제공
    fun addTestSession(session: RehabSession) {
        _rehabSessions.value = _rehabSessions.value + session
    }

    override suspend fun addRehabSession(session: RehabSession): Flow<Unit> {
        addTestSession(session.copy(id = session.id.ifEmpty { UUID.randomUUID().toString() }))
        return flowOf(Unit)
    }

    override suspend fun getRehabHistory(userId: String): Flow<List<RehabSession>> {
        return _rehabSessions.map { sessions -> sessions.filter { it.userId == userId } }
    }
}