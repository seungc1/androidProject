// app/src/test/java/com/example/androidproject/data/repository/FakeDietSessionRepository.kt
package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.repository.DietSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

class FakeDietSessionRepository : DietSessionRepository {
    private val _dietSessions = MutableStateFlow<List<DietSession>>(emptyList())

    // 테스트 시 쉽게 데이터를 추가할 수 있도록 함수 제공
    fun addTestSession(session: DietSession) {
        _dietSessions.value = _dietSessions.value + session
    }

    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        addTestSession(session.copy(id = session.id.ifEmpty { UUID.randomUUID().toString() }))
        return flowOf(Unit)
    }

    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        return _dietSessions.map { sessions -> sessions.filter { it.userId == userId } }
    }
}