package com.example.androidproject

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
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
            // (userId가 없으면 스플래시/로그인 화면으로 - 자동 처리됨)
        } else {
            viewModel.loadDataForUser(userId)
        }

        setupNavigation()
        observeProfileStatus()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = binding.bottomNavigationView

        // (★수정★) 기본 NavigationUI.setupWithNavController 대신
        // setOnItemSelectedListener를 직접 구현하여 커스텀 동작(상태 리셋)을 정의합니다.

        // 1. 초기 설정 (선택된 아이템 표시 등)은 setupWithNavController로 하되,
        //    리스너를 덮어씌우는 방식입니다.
        NavigationUI.setupWithNavController(bottomNav, navController)

        // 2. 리스너 재정의
        bottomNav.setOnItemSelectedListener { item ->
            // 현재 보고 있는 탭을 다시 누른 경우 (Reselection) -> popBackStack
            if (item.itemId == navController.currentDestination?.id) {
                navController.popBackStack(item.itemId, false)
                return@setOnItemSelectedListener true
            }

            val builder = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(navController.graph.startDestinationId, false)

            if (item.itemId == R.id.navigation_profile) {
                // (★핵심★) '개인정보' 탭 클릭 시: 상태를 복원하지 않음 (restoreState = false)
                // 이렇게 하면 이전에 '수정 화면'에 있었더라도 '초기 화면'(내 정보)으로 리셋됩니다.
                builder.setRestoreState(false)
            } else {
                // 다른 탭 클릭 시: 상태를 복원함 (기존 동작 유지)
                builder.setRestoreState(true)
            }

            // 네비게이션 수행
            try {
                navController.navigate(item.itemId, null, builder.build())
            } catch (e: Exception) {
                // 네비게이션 실패 시 (예: 이미 해당 화면일 때) 무시
                return@setOnItemSelectedListener false
            }

            return@setOnItemSelectedListener true
        }

        // (참고) 재선택 리스너는 위에서 처리했으므로 별도 설정 불필요하지만,
        // setupWithNavController가 덮어쓸 수 있으니 명시적으로 둡니다.
        bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, false)
        }
    }

    private fun observeProfileStatus() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (!state.isLoading) {

                        // --- 기존 코드 수정 시작: 프로필 미완성 시 탭바 숨김 로직을 제거하고,
                        //     항상 탭바를 보이게 합니다. ---
                        if (!state.isProfileComplete) {
                            // [수정] 탭바를 숨기는 View.GONE 로직을 주석 처리하여 항상 보이게 함
                            // binding.bottomNavigationView.visibility = View.GONE

                            val currentDest = navController.currentDestination?.id
                            if (currentDest != R.id.profileEditFragment) {
                                // 프로필 입력 강제 이동 로직은 유지 (사용자에게 프로필 입력 필요함을 알림)
                                Toast.makeText(this@MainActivity, "초기 설정을 위해 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                                try {
                                    navController.navigate(R.id.profileEditFragment)
                                } catch (_: Exception) { /* 무시 */ }
                            }
                        }

                        // [추가] 로딩이 완료되면 내비게이션 바를 항상 보이도록 설정
                        binding.bottomNavigationView.visibility = View.VISIBLE
                        // --- 기존 코드 수정 끝 ---
                    }
                }
            }
        }
    }
}