package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.Injury
import kotlinx.coroutines.flow.Flow

interface InjuryRepository {
    suspend fun upsertInjury(injury: Injury, userId: String)
    fun getInjuryById(injuryId: String): Flow<Injury?>
    fun getInjuriesForUser(userId: String): Flow<List<Injury>>
}