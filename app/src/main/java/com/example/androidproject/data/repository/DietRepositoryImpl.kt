package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.repository.DietRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DietRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : DietRepository {
    override suspend fun upsertDiets(diets: List<Diet>) {
        localDataSource.upsertDiets(diets.map { it.toEntity() })
    }

    override fun getDietById(dietId: String): Flow<Diet?> {
        return localDataSource.getDietById(dietId).map { it?.toDomain() }
    }
}