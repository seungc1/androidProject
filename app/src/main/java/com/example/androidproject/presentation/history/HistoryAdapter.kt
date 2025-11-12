package com.example.androidproject.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.androidproject.databinding.ItemDietBinding // (★필수★) '식단 견본'의 ViewBinding import
import com.example.androidproject.databinding.ItemExerciseBinding // (★필수★) '운동 견본'의 ViewBinding import
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import java.text.SimpleDateFormat // (★수정★) 'API 레벨 26' 문제 해결을 위해 'SimpleDateFormat' (API 1) 사용
// import java.time.LocalDateTime // (★수정★)
// import java.time.format.DateTimeFormatter // (★수정★)
import java.util.Locale
import java.util.Date // (★수정★) 님의 '필드값' 및 '가이드라인' 원칙 4에 따라 'Date' 사용

/**
 * [새 파일] - '기록' 탭의 '목록 관리자'
 * '운동 기록'(RehabSession)과 '식단 기록'(DietSession)을
 * '하나의' '목록'(RecyclerView)에 '둘 다' '표시'하기 위한 '스마트' 어댑터입니다.
 * (팀원 1의 로드맵 Phase 4)
 */

// 1. '데이터 묶음' 정의: '운동'이거나 '식단'일 수 있는 '봉투'
// (★수정★) '필드값' 모델('Date')과 '타입'을 '일치'시킵니다.
sealed class HistoryItem {
    data class Exercise(val session: RehabSession) : HistoryItem() {
        override val id: String = session.id
        override val dateTime: Date = session.dateTime // (★수정★) LocalDateTime -> Date
    }
    data class Diet(val session: DietSession) : HistoryItem() {
        override val id: String = session.id
        override val dateTime: Date = session.dateTime // (★수정★) LocalDateTime -> Date
    }

    // (★핵심★) 'DiffUtil'과 '정렬'을 위해 '공통' 필드를 '추상화'합니다.
    abstract val id: String
    abstract val dateTime: Date // (★수정★) LocalDateTime -> Date
}

// 2. '목록 관리자' (Adapter) 정의
class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    /**
     * '데이터'가 '운동'인지 '식단'인지 '구별'해서 '타입'을 '반환'합니다.
     */
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HistoryItem.Exercise -> VIEW_TYPE_EXERCISE
            is HistoryItem.Diet -> VIEW_TYPE_DIET
            else -> throw IllegalArgumentException("Unknown item type")
        }
    }

    /**
     * '타입'에 맞는 '한 줄 견본'(XML)을 '생성'합니다.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // '타입'이 '운동'이면 'item_exercise.xml'을, '식단'이면 'item_diet.xml'을 '사용'합니다.
        val binding = when (viewType) {
            VIEW_TYPE_EXERCISE -> ItemExerciseBinding.inflate(inflater, parent, false)
            VIEW_TYPE_DIET -> ItemDietBinding.inflate(inflater, parent, false)
            else -> throw IllegalArgumentException("Invalid view type")
        }
        return HistoryViewHolder(binding)
    }

    /**
     * '데이터'('HistoryItem')를 '한 줄 견본'(ViewHolder)에 '연결'합니다.
     */
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * '한 줄 견본'(XML)의 UI 요소들을 '관리'하는 '뷰 홀더'입니다.
     * (★핵심★) 'ViewBinding'의 '공통 부모'인 'ViewBinding'을 '사용'합니다.
     */
    inner class HistoryViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

        // (★수정★) 'SimpleDateFormat' '포맷터'로 '변경' (예: "오후 3:05")
        private val timeFormatter = SimpleDateFormat("a h:mm", Locale.KOREA)

        /**
         * '데이터'('HistoryItem')를 'UI'('한 줄 견본')에 '연결'합니다.
         */
        fun bind(item: HistoryItem) {
            when (item) {
                // '운동' 데이터가 '운동' 견본에 '연결'되는 경우
                is HistoryItem.Exercise -> {
                    val exerciseBinding = binding as ItemExerciseBinding // '형 변환'
                    val session = item.session

                    // (★수정★) '기록' 탭에 맞게 '운동 견본' UI를 '재활용'합니다.
                    // (임시) 님의 '필드값' Exercise.kt에 'name'이 있으므로,
                    // '핵심 두뇌'(ViewModel)가 'session.exerciseId'를 '운동 이름'으로 '변환'해줘야 합니다.
                    exerciseBinding.exerciseNameTextView.text = "운동: ${session.exerciseId}" // (임시 - '운동 이름'으로 '변환' 필요)

                    // (★수정★) 'Unresolved reference 'userFeedback'' 오류 '해결'
                    exerciseBinding.exerciseDetailTextView.text =
                        "${session.sets} 세트 / ${session.reps} 회" // (피드백: ${session.userFeedback ?: "없음"})"

                    // '기록' 탭에서는 '체크박스' 대신 '시간'을 보여줍니다.
                    exerciseBinding.exerciseStatusCheckBox.text = timeFormatter.format(session.dateTime) // (★수정★)
                    exerciseBinding.exerciseStatusCheckBox.isClickable = false // (체크 불가능)
                    exerciseBinding.exerciseStatusCheckBox.isChecked = false // (체크 상태 해제)
                }

                // '식단' 데이터가 '식단' 견본에 '연결'되는 경우
                is HistoryItem.Diet -> {
                    val dietBinding = binding as ItemDietBinding // '형 변환'
                    val session = item.session

                    // (★수정★) '기록' 탭에 맞게 '식단 견본' UI를 '재활용'합니다.
                    // (임시) 님의 '필드값' Diet.kt에 'foodName'이 있으므로,
                    // '핵심 두뇌'(ViewModel)가 'session.dietId'를 '음식 이름'으로 '변환'해줘야 합니다.
                    dietBinding.dietNameTextView.text = "식단: ${session.dietId}" // (임시 - '음식 이름'으로 '변환' 필요)
                    dietBinding.dietDetailTextView.text =
                        "섭취량: ${session.actualQuantity} ${session.actualUnit} (만족도: ${session.userSatisfaction ?: "없음"})"

                    // '기록' 탭에서는 '칼로리' 대신 '시간'을 보여줍니다.
                    dietBinding.dietCaloriesTextView.text = timeFormatter.format(session.dateTime) // (★수정★)
                }
            }
        }
    }

    // (★수정★) 'const val' '문법 오류' 해결을 위해 'companion object'를 '적용'합니다.
    companion object {
        private const val VIEW_TYPE_EXERCISE = 1
        private const val VIEW_TYPE_DIET = 2
    }
}

/**
 * '목록'이 '효율적으로' '새로고침'될 수 있도록 돕는 'DiffUtil'입니다.
 */
class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
    override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem.id == newItem.id // '추상화'한 'id'로 '비교'
    }

    override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
        return oldItem == newItem
    }
}