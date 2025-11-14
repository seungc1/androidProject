package com.example.androidproject.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.androidproject.data.local.dao.ExerciseDao
import com.example.androidproject.data.local.dao.UserDao
import com.example.androidproject.data.local.entity.ExerciseEntity
import com.example.androidproject.data.local.entity.UserEntity

// @Database 어노테이션으로 이 클래스가 Room 데이터베이스임을 선언합니다.
@Database(
    // 데이터베이스가 포함할 Entity(테이블) 목록을 배열로 전달합니다.
    entities = [UserEntity::class, ExerciseEntity::class],
    // 데이터베이스 버전
    version = 1,
    // 스키마 정보를 파일로 내보낼지 여부
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    //Room이 추상 함수 구현하여 UserDao 인스턴스 제공
    abstract fun userDao(): UserDao
    //Room이 추상 함수 구현하여 ExerciseDao 인스턴스 제공
    abstract fun exerciseDao(): ExerciseDao

    // DiertEntity, RehabSessionEntity 등 추가 예정
}
