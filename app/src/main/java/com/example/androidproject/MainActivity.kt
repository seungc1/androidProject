// app/src/main/java/com/example/androidproject/MainActivity.kt
package com.example.androidproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment // (성준민 추가) '지도'를 찾기 위해 import
import androidx.navigation.ui.NavigationUI // (성준민 추가) '지도'와 '툴바'를 연결하기 위해 import
import com.example.androidproject.databinding.ActivityMainBinding // (성준민 추가) ViewBinding import
import dagger.hilt.android.AndroidEntryPoint

/**
 * [오류 수정 2/2]
 * '껍데기(Activity)'이자 모든 Fragment를 담는 '두뇌'입니다.
 * '하단 툴바'와 '지도'를 '연결'하는 최종 코드가 포함되어 있습니다.
 */
@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
// Hilt를 사용하는 MainActivity라면  @AndroidEntryPoint 어노테이션도 다시 추가해주세요.
// @AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // (성준민 추가) ViewBinding 설정
    // 님의 'activity_main.xml' 파일을 기반으로 자동 생성된 클래스입니다.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // (성준민 추가) ViewBinding을 사용하여 activity_main.xml (껍데기 UI)를 연결합니다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // R.layout.activity_main 대신 binding.root를 사용

        // (성준민 추가) '연결성'의 마지막 단계: '하단 툴바'와 '지도'를 연결합니다.
        setupNavigation()
    }

    /**
     * (성준민 추가)
     * '하단 툴바'(BottomNavigationView)와 '내비게이션 지도'(nav_graph)를
     * 연결하는 '최종 연결' 함수입니다.
     *
     * ★★★
     * 이 코드가 있어야, "앱이 실행되면" '지도'의 시작 화면(홈)이
     * '문틀'에 표시되고, [홈], [기록] 버튼이 작동합니다.
     * ★★★
     */
    private fun setupNavigation() {
        // 1. '문틀'(NavHostFragment)을 activity_main.xml에서 찾습니다.
        //    (오류가 났던 'nav_host_fragment' ID 참조)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // 2. '문틀'에서 '내비게이션 관리자'(NavController)를 가져옵니다.
        val navController = navHostFragment.navController

        // 3. '하단 툴바'(BottomNavigationView)를 activity_main.xml에서 찾습니다.
        //    (오류가 났던 'bottomNavigationView' ID 참조)
        val bottomNav = binding.bottomNavigationView

        // 4. (★핵심★) '하단 툴바'와 '내비게이션 관리자'를 연결합니다.
        //    (메뉴 ID와 '지도'의 ID가 일치해야 합니다)
        NavigationUI.setupWithNavController(bottomNav, navController)
    }
}