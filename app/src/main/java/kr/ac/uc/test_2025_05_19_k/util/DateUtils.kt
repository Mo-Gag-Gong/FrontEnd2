package kr.ac.uc.test_2025_05_19_k.util

import android.util.Log
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * "yyyy-MM-dd" 형식의 문자열을 LocalDate 객체로 변환합니다.
 */
fun toDate(dateString: String?): LocalDate? {
    if (dateString.isNullOrEmpty()) {
        return null
    }
    return try {
        LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        null
    }
}

/**
 * 서버에서 받은 UTC 기반 시간 문자열을 시스템 기본 시간대(KST)의 ZonedDateTime 객체로 변환합니다.
 */
private fun convertToSystemDefaultZonedDateTime(dateTimeString: String?): ZonedDateTime? {
    if (dateTimeString.isNullOrBlank()) return null

    // 1. 첫 번째 시도: 표준 UTC 형식 (e.g., "2023-10-27T10:15:30.123Z")
    try {
        val instant = Instant.parse(dateTimeString)
        return instant.atZone(ZoneId.systemDefault())
    } catch (e: Exception) {
        // 파싱 실패 시 다음 형식으로 넘어갑니다.
    }

    // 2. 두 번째 시도: 시간대 정보가 없는 형식 (e.g., "2023-10-27T10:15:30")
    // 이 문자열을 UTC 시간으로 간주하고 시스템 기본 시간대(KST)로 변환합니다.
    try {
        val localDateTime = LocalDateTime.parse(dateTimeString)
        return localDateTime.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault())
    } catch (e: Exception) {
        // 최종 실패
        Log.w("DateUtils", "Failed to parse date-time string with multiple formats: $dateTimeString")
        return null
    }
}

/**
 * 두 UTC 시간 문자열이 같은 날짜인지 비교합니다.
 */
fun isSameDay(dateTimeString1: String?, dateTimeString2: String?): Boolean {
    val zonedDateTime1 = convertToSystemDefaultZonedDateTime(dateTimeString1)
    val zonedDateTime2 = convertToSystemDefaultZonedDateTime(dateTimeString2)

    if (zonedDateTime1 == null || zonedDateTime2 == null) return false

    return zonedDateTime1.toLocalDate().isEqual(zonedDateTime2.toLocalDate())
}

/**
 * 날짜 구분선을 "2025년 6월 15일 일요일" 형식으로 포맷합니다.
 */
fun formatSeparatorDate(dateTimeString: String?): String {
    val zonedDateTime = convertToSystemDefaultZonedDateTime(dateTimeString)
    return zonedDateTime?.let {
        val dayOfWeek = it.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
        "${it.year}년 ${it.monthValue}월 ${it.dayOfMonth}일 $dayOfWeek"
    } ?: "알 수 없는 날짜"
}

/**
 * 메시지 시간을 "오후 9:39" 형식으로 포맷합니다.
 */
fun formatMessageTime(dateTimeString: String?): String {
    val zonedDateTime = convertToSystemDefaultZonedDateTime(dateTimeString)
    return zonedDateTime?.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)) ?: ""
}