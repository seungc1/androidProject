package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.repository.DietSessionRepository
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
class DietSessionRepositoryImpl @Inject constructor() : DietSessionRepository {

    private val _dietSessions = MutableStateFlow<List<DietSession>>(emptyList())
    private val dietSessions: Flow<List<DietSession>> = _dietSessions.asSharedFlow()

    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        val newSession = session.copy(id = session.id.ifEmpty { UUID.randomUUID().toString() })
        _dietSessions.value = _dietSessions.value + newSession
        return flowOf(Unit)
    }

    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        return _dietSessions.map { sessions -> sessions.filter { it.userId == userId } }
    }

    override suspend fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSession>> {
        return _dietSessions.map { sessions ->
            sessions.filter {
                it.userId == userId && !it.dateTime.before(startDate) && !it.dateTime.after(endDate)
            }
        }
    }
}