package kr.ac.uc.test_2025_05_19_k.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// String을 Date 객체로 변환하는 확장 함수
fun String.toDate(): Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}