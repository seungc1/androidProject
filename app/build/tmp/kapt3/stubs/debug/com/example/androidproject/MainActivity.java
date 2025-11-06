package com.example.androidproject;

/**
 * [오류 수정 2/2]
 * '껍데기(Activity)'이자 모든 Fragment를 담는 '두뇌'입니다.
 * '하단 툴바'와 '지도'를 '연결'하는 최종 코드가 포함되어 있습니다.
 */
@dagger.hilt.android.AndroidEntryPoint
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0014J\b\u0010\t\u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/example/androidproject/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/example/androidproject/databinding/ActivityMainBinding;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setupNavigation", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.example.androidproject.databinding.ActivityMainBinding binding;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
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
    private final void setupNavigation() {
    }
}