package com.example.androidproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment // (★필수★) '지도'를 찾기 위해 import
import androidx.navigation.ui.NavigationUI // (★필수★) '지도'와 '툴바'를 연결하기 위해 import
import com.example.androidproject.databinding.ActivityMainBinding // (★필수★) ViewBinding import
import dagger.hilt.android.AndroidEntryPoint

/**
 * [연결성 - 최종 단계]
 * 앱의 '껍데기(Activity)'이자 모든 Fragment를 담는 '두뇌'입니다.
 *
 * (Git 충돌/덮어쓰기 문제 해결을 위해 '최종 연결' 코드를
 * 다시 덮어씁니다.)
 */
@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
class MainActivity : AppCompatActivity() {

    // (★필수★) ViewBinding 설정
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // (★필수★) ViewBinding을 사용하여 activity_main.xml (껍데기 UI)를 연결합니다.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // R.layout.activity_main 대신 binding.root를 사용

        // (★필수★) '연결성'의 마지막 단계: '하단 툴바'와 '지도'를 연결합니다.
        setupNavigation()
    }

    /**
     * (★필수★)
     * '하단 툴바'(BottomNavigationView)와 '내비게이션 지도'(nav_graph)를
     * 연결하는 '최종 연결' 함수입니다.
     *
     * 이 코드가 있어야, "앱이 실행되면" '지도'의 시작 화면(홈)이
     * '문틀'에 표시되고, [홈], [기록] 버튼이 작동합니다.
     */
    private fun setupNavigation() {
        // 1. '문틀'(NavHostFragment)을 activity_main.xml에서 찾습니다.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // 2. '문틀'에서 '내비게이션 관리자'(NavController)를 가져옵니다.
        val navController = navHostFragment.navController

        // 3. '하단 툴바'(BottomNavigationView)를 activity_main.xml에서 찾합니다.
        val bottomNav = binding.bottomNavigationView

        // 4. (★핵심★) '하단 툴바'와 '내비게이션 관리자'를 연결합니다.
        //    (메뉴 ID와 '지도'의 ID가 일치해야 합니다)
        NavigationUI.setupWithNavController(bottomNav, navController)
    }
}