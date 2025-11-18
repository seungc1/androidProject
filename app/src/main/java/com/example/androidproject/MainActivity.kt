package com.example.androidproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.androidproject.databinding.ActivityMainBinding
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: RehabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getStringExtra("USER_ID")

        if (userId.isNullOrEmpty()) {
            // (userId가 없으면 스플래시/로그인 화면으로)
            // 여기서 처리하지 않아도 SplashActivity가 처리함
        } else {
            viewModel.loadDataForUser(userId)
        }

        setupNavigation()

        // (★추가★) 프로필 입력 강제 로직 (Global Guard)
        observeProfileStatus()
    }

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

    /**
     * (★핵심★) 사용자의 프로필 상태를 실시간으로 감시합니다.
     */
    private fun observeProfileStatus() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->

                    // 1. 로딩이 끝났고
                    if (!state.isLoading) {
                        // 2. 프로필이 미완성("신규 사용자") 상태라면
                        if (!state.isProfileComplete) {

                            // 3. 현재 화면이 이미 '수정 화면'인지 확인
                            val currentDest = navController.currentDestination?.id
                            if (currentDest != R.id.profileEditFragment) {

                                // 4. 아니라면 강제로 '수정 화면'으로 이동
                                Toast.makeText(this@MainActivity, "원활한 재활을 위해 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()

                                try {
                                    // (수정) 액션 ID 대신 목적지 Fragment ID로 직접 이동
                                    navController.navigate(R.id.profileEditFragment)
                                } catch (_: Exception) {
                                    // (수정) 사용하지 않는 변수는 '_'로 처리
                                    // 현재 위치에서 갈 수 없는 경우 (예: 이미 이동 중) 무시
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}