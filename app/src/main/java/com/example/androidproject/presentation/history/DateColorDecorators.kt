package com.example.androidproject.presentation.history

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.HashSet

/**
 * [새 파일 4/6] - '날짜' '데코레이터' 1
 * '데이터가 있는' 날짜의 '글자'를 '검은색'과 '굵게' '표시'합니다.
 */
class EnabledDateDecorator(private val dates: HashSet<CalendarDay>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day) // '데이터가 있는' '목록'에 '포함'된 '날'만 '꾸미기'
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.BLACK))
        view.addSpan(StyleSpan(Typeface.BOLD))
    }
}

/**
 * [새 파일 5/6] - '날짜' '데코레이터' 2
 * '데이터가 없는' 날짜의 '글자'를 '연한 회색'으로 '표시'합니다.
 */
class DisabledDateDecorator(
    private val allDaysInMonth: HashSet<CalendarDay>,
    private val recordedDays: HashSet<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        // '현재' '달' '날짜' '중'에서, '기록된' '날짜'가 '아닌' '날'만 '꾸미기'
        return allDaysInMonth.contains(day) && !recordedDays.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.LTGRAY))
        // (클릭은 '막지' '않음' - 빈 '기록'을 '보여줘야' '함')
    }
}