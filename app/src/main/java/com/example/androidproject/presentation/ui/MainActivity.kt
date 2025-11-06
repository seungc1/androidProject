// app/src/main/java/com/example/androidproject/presentation/ui/MainActivity.kt
package com.example.androidproject.presentation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject.R
import com.example.androidproject.presentation.viewmodel.RehabViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt가 이 Activity에 의존성을 주입하도록 지시합니다.
class MainActivity : AppCompatActivity() {

    // Hilt를 통해 ViewModel을 주입받습니다.
    private val rehabViewModel: RehabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // 기본적인 레이아웃 설정

        // TODO: 여기에 UI 요소를 초기화하고 ViewModel과 연결하는 코드를 작성할 예정입니다.
        // 예를 들어, 버튼 클릭 시 ViewModel의 함수 호출, LiveData 관찰 등.
    }
}