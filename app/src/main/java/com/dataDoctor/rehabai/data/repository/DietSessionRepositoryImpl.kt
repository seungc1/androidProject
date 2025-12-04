package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.local.datasource.LocalDataSource
import com.dataDoctor.rehabai.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.dataDoctor.rehabai.data.mapper.toDomain
import com.dataDoctor.rehabai.data.mapper.toEntity
import com.dataDoctor.rehabai.domain.model.DietSession
import com.dataDoctor.rehabai.domain.repository.DietSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first // [추가]
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietSessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource // (★ 추가 주입)
) : DietSessionRepository {

    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        android.util.Log.d("DIET_REPO", "addDietSession called: ${session.id}, foodName=${session.foodName}")
        
        try {
            // 1. Firebase 저장
            android.util.Log.d("DIET_REPO", "Saving to Firebase...")

            // [추가] 로컬 이미지인 경우 Firebase Storage에 업로드하고 URL 교체
            var sessionToSave = session
            val photoUrl = session.photoUrl
            if (photoUrl != null && photoUrl.startsWith("file://")) {
                try {
                    val downloadUrl = firebaseDataSource.uploadImage(session.userId, android.net.Uri.parse(photoUrl))
                    sessionToSave = session.copy(photoUrl = downloadUrl)
                    android.util.Log.d("DIET_REPO", "Image uploaded to Firebase Storage: $downloadUrl")
                } catch (e: Exception) {
                    android.util.Log.e("DIET_REPO", "Image upload failed: ${e.message}")
                    // 업로드 실패 시 로컬 URI 유지 (오프라인 등)
                }
            }

            firebaseDataSource.addDietSession(sessionToSave)
            android.util.Log.d("DIET_REPO", "Firebase save complete")
            
            // 2. Local 저장
            android.util.Log.d("DIET_REPO", "Saving to local DB...")
            localDataSource.addDietSession(session.toEntity())
            android.util.Log.d("DIET_REPO", "Local DB save complete")
        } catch (e: Exception) {
            android.util.Log.e("DIET_REPO", "Error in addDietSession: ${e.message}", e)
            throw e
        }
        
        return flowOf(Unit)
    }

    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        // 1. 로컬 데이터 구독
        val localData = localDataSource.getDietHistory(userId).map { entityList ->
            entityList.map { it.toDomain() }
        }

        // 2. 서버 데이터 동기화
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteSessions = firebaseDataSource.getDietHistory(userId)
                
                // [추가] 삭제 동기화 로직
                // 로컬에만 있고 리모트에는 없는 데이터(삭제된 데이터)를 찾아서 로컬에서도 삭제
                val localSessions = localDataSource.getDietHistory(userId).first() // 현재 로컬 데이터 스냅샷
                val remoteIds = remoteSessions.map { it.id }.toSet()
                
                localSessions.forEach { localEntity ->
                    if (!remoteIds.contains(localEntity.id)) {
                        android.util.Log.d("DIET_SYNC", "Deleting local session not found in remote: ${localEntity.id}")
                        localDataSource.deleteDietSessionById(localEntity.id)
                    }
                }

                // 리모트 데이터 로컬에 추가/업데이트
                remoteSessions.forEach { session ->
                    localDataSource.addDietSession(session.toEntity())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return localData
    }

    override suspend fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSession>> {
        return localDataSource.getDietSessionsBetween(userId, startDate, endDate).map { entityList ->
            entityList.map { it.toDomain() }
        }
    }

    override suspend fun getDietSessionById(id: String): Flow<DietSession?> {
        return localDataSource.getDietSessionById(id).map { it?.toDomain() }
    }
}