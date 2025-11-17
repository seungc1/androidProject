package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.repository.InjuryRepository // 👈 (Interface import)
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InjuryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : InjuryRepository {
    override suspend fun upsertInjury(injury: Injury, userId: String) {
        localDataSource.upsertInjury(injury.toEntity(userId))
    }

    override fun getInjuryById(injuryId: String): Flow<Injury?> {
        return localDataSource.getInjuryById(injuryId).map { it?.toDomain() }
    }

    override fun getInjuriesForUser(userId: String): Flow<List<Injury>> {
        return localDataSource.getInjuriesForUser(userId).map { list -> list.map { it.toDomain() } }
    }
}