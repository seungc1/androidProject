package com.example.androidproject

import android.os.Bundle
import android.view.View // (★필수★) View.GONE, View.VISIBLE 사용을 위해
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
            // (userId가 없으면 스플래시/로그인 화면으로 - 자동 처리됨)
        } else {
            viewModel.loadDataForUser(userId)
        }

        setupNavigation()
        observeProfileStatus() // 프로필 상태 감시 시작
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

    private fun observeProfileStatus() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    if (!state.isLoading) {

                        // (★핵심★) 프로필 미완성 상태 처리
                        if (!state.isProfileComplete) {

                            // 1. 하단 탭 숨기기 (이동 차단)
                            binding.bottomNavigationView.visibility = View.GONE

                            // 2. 현재 화면이 이미 '수정 화면'이 아니면 강제 이동
                            val currentDest = navController.currentDestination?.id
                            if (currentDest != R.id.profileEditFragment) {
                                Toast.makeText(this@MainActivity, "초기 설정을 위해 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                                try {
                                    navController.navigate(R.id.profileEditFragment)
                                } catch (_: Exception) { }
                            }

                        } else {
                            // (★핵심★) 프로필 완성 시 하단 탭 다시 보이기
                            binding.bottomNavigationView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}