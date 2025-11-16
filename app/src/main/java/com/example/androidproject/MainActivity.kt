package com.example.androidproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment // (★필수★)
import androidx.navigation.ui.NavigationUI // (★필수★)
import com.example.androidproject.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * '상세' 화면에서 '하단 툴바'의 '홈' 버튼을 '다시' 눌렀을 때,
 * '홈' 화면으로 '복귀'하도록 'setOnItemReselectedListener'를 '추가'합니다.
 */
@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // (★필수★) '하단 툴바'와 '지도'를 '연결'합니다.
        setupNavigation()
    }

    /**
     * '하단 툴바'(BottomNavigationView)와 '내비게이션 지도'(nav_graph)를
     * '연결'하고, '재선택' 이벤트를 '처리'합니다.
     */
    private fun setupNavigation() {
        // 1. '문틀'(NavHostFragment)을 activity_main.xml에서 찾습니다.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // 2. '문틀'에서 '내비게이션 관리자'(NavController)를 가져옵니다.
        val navController = navHostFragment.navController

        // 3. '하단 툴바'(BottomNavigationView)를 activity_main.xml에서 찾습니다.
        val bottomNav = binding.bottomNavigationView

        // 4. (★기존★) '하단 툴바'와 '내비게이션 관리자'를 '기본 연결'합니다.
        // (이 코드는 '다른' 탭(예: 기록, 개인정보)을 눌렀을 때의 '이동'을 처리합니다.)
        NavigationUI.setupWithNavController(bottomNav, navController)


        // 5. (★ 추가 ★) '현재 탭'을 '다시' 눌렀을 때의 '동작'을 '정의'합니다.
        // (이 코드가 '상세' 화면 -> '홈' 화면 복귀를 '처리'합니다.)
        bottomNav.setOnItemReselectedListener { item ->
            // (예: '홈' 탭이 '선택'된 상태에서 '홈' 아이콘을 '다시' 누른 경우)

            // 'popBackStack'을 '호출'하여 해당 탭의 '첫' 화면(예: 'navigation_home') '전까지'
            // '모든' '상세' 화면(예: ExerciseDetailFragment)을 '스택에서 제거'(뒤로가기)합니다.
            navController.popBackStack(item.itemId, inclusive = false)
        }
    }
}