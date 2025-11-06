// app/src/main/java/com/example/androidproject/MainActivity.kt
package com.example.androidproject

import android.os.Bundle
// import androidx.activity.enableEdgeToEdge // 이 줄은 제거하거나 주석 처리
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Hilt를 사용하는 MainActivity라면  @AndroidEntryPoint 어노테이션도 다시 추가해주세요.
// @AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() // 이 줄은 제거하거나 주석 처리
        setContentView(R.layout.activity_main)

        // R.id.main은 activity_main.xml에 있는 최상위 뷰의 ID여야 합니다.
        // 현재는 레이아웃 파일이 비어있을 수 있으므로, 이 부분도 잠시 주석 처리할 수 있습니다.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}