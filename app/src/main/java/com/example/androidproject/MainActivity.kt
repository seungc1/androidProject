package com.example.androidproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels // (★ 추가 ★) 'by viewModels' '사용'
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.androidproject.databinding.ActivityMainBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel // (★ 추가 ★)
import dagger.hilt.android.AndroidEntryPoint

/**
 * [수정 파일 2/2] '메인' 화면 '두뇌'
 * (★ 수정 ★) '로그인' '화면'에서 '전달'받은 'USER_ID'를 '사용'하여
 * 'RehabViewModel'의 '데이터' '로드'를 '시작'시킵니다.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // (★ 추가 ★) 'RehabViewModel'을 '주입'
    private val viewModel: RehabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. (★ 추가 ★) 'Splash' '또는' 'Login' '화면'에서 '전달'받은 'USER_ID'를 '가져옵니다'.
        val userId = intent.getStringExtra("USER_ID")

        // 2. (★ 추가 ★) 'USER_ID'가 '있으면' '데이터' '로드'를 '시작'
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "오류: 사용자 ID 없이 시작되었습니다.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            // (★ 핵심 ★) 'ViewModel'에게 '데이터' '로드'를 '명령'합니다.
            viewModel.loadDataForUser(userId)
        }

        // 3. (★기존★) '하단 툴바'와 '지도'를 '연결'합니다.
        setupNavigation()
    }

    // (setupNavigation - 수정 없음)
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigationView

        NavigationUI.setupWithNavController(bottomNav, navController)

        bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, inclusive = false)
        }
    }
}