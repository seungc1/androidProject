package com.example.androidproject.presentation.history

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
// (★ 수정 ★) 'HashSet' '대신' '범용' 'Set' 'import'
import kotlin.collections.Set

/**
 * [수정 파일 4/6] - '날짜' '데코레이터' 1
 * (★ 수정 ★) 'HashSet<CalendarDay>' -> 'Set<CalendarDay>'로 '변경'
 */
class EnabledDateDecorator(private val dates: Set<CalendarDay>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.BLACK))
        view.addSpan(StyleSpan(Typeface.BOLD))
    }
}

/**
 * [수정 파일 5/6] - '날짜' '데코레이터' 2
 * (★ 수정 ★) 'HashSet<CalendarDay>' -> 'Set<CalendarDay>'로 '변경'
 */
class DisabledDateDecorator(
    private val allDaysInMonth: Set<CalendarDay>,
    private val recordedDays: Set<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        // '현재' '달' '날짜' '중'에서, '기록된' '날짜'가 '아닌' '날'만 '꾸미기'
        return allDaysInMonth.contains(day) && !recordedDays.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.LTGRAY))
    }
}