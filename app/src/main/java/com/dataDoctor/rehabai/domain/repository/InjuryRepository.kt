package com.dataDoctor.rehabai.domain.repository

import com.dataDoctor.rehabai.domain.model.Injury
import kotlinx.coroutines.flow.Flow

interface InjuryRepository {
    suspend fun upsertInjury(injury: Injury, userId: String)
    fun getInjuryById(injuryId: String): Flow<Injury?>
    fun getInjuriesForUser(userId: String): Flow<List<Injury>>
}