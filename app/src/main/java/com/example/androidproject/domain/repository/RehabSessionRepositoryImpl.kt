package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RehabSessionRepositoryImpl @Inject constructor() : RehabSessionRepository {

    private val _rehabSessions = MutableStateFlow<List<RehabSession>>(emptyList())
    private val rehabSessions: Flow<List<RehabSession>> = _rehabSessions.asSharedFlow()

    override suspend fun addRehabSession(session: RehabSession): Flow<Unit> {
        val newSession = session.copy(id = session.id.ifEmpty { UUID.randomUUID().toString() })
        _rehabSessions.value = _rehabSessions.value + newSession
        return flowOf(Unit)
    }

    override suspend fun getRehabHistory(userId: String): Flow<List<RehabSession>> {
        return _rehabSessions.map { sessions -> sessions.filter { it.userId == userId } }
    }
}