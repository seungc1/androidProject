package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.local.datasource.LocalDataSource
import com.dataDoctor.rehabai.data.remote.datasource.FirebaseDataSource
import com.dataDoctor.rehabai.data.mapper.toDomain
import com.dataDoctor.rehabai.data.mapper.toEntity
import com.dataDoctor.rehabai.domain.model.Injury
import com.dataDoctor.rehabai.domain.repository.InjuryRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class InjuryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource
) : InjuryRepository {

    override suspend fun upsertInjury(injury: Injury, userId: String) {
        try {
            firebaseDataSource.upsertInjury(userId, injury)
            localDataSource.upsertInjury(injury.toEntity(userId))
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    // (★ 수정 ★) 로컬 반환 + 백그라운드 서버 동기화
    override fun getInjuryById(injuryId: String): Flow<Injury?> {
        // 1. 백그라운드에서 최신 정보 가져와서 로컬 DB 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteInjury = firebaseDataSource.getInjury(injuryId)
                if (remoteInjury != null) {
                    // userId가 필요하지만, 여기선 현재 로그인된 유저라고 가정하거나
                    // remoteInjury 데이터를 믿고 저장.
                    // 안전을 위해 현재 로그인된 ID를 가져옵니다.
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.email?.split("@")?.get(0)
                        ?: FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

                    // (참고: 원래는 userId를 인자로 받아야 정확하지만,
                    //  구조상 어려우면 일단 현재 유저 기준으로 저장)
                    localDataSource.upsertInjury(remoteInjury.toEntity(currentUserId))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. 로컬 데이터 반환 (서버 데이터가 DB에 들어오면 Flow가 자동으로 갱신됨)
        return localDataSource.getInjuryById(injuryId).map { it?.toDomain() }
    }

    override fun getInjuriesForUser(userId: String): Flow<List<Injury>> {
        val localFlow = localDataSource.getInjuriesForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteInjuries = firebaseDataSource.getInjuries(userId)
                if (remoteInjuries.isNotEmpty()) {
                    remoteInjuries.forEach { injury ->
                        localDataSource.upsertInjury(injury.toEntity(userId))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return localFlow
    }
}