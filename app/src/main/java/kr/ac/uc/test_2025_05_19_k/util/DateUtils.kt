package kr.ac.uc.test_2025_05_19_k.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// String을 Date 객체로 변환하는 확장 함수
fun String.toDate(): Date? {
    // ▼▼▼ [수정] 날짜 포맷을 서버 데이터 형식과 일치시킵니다. ▼▼▼
    // "T"는 문자 리터럴이므로 작은따옴표로 감싸고, 소수점 이하 초는 SSS로 표현합니다.
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.KOREA)
    return try {
        format.parse(this)
    } catch (e: Exception) {
        // 만약 소수점 이하가 없는 형식도 대비하려면 여기서 한 번 더 다른 포맷으로 파싱할 수 있습니다.
        try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).parse(this)
        } catch (e2: Exception) {
            null
        }
    }
}

fun isSameDay(dateString1: String?, dateString2: String?): Boolean {
    if (dateString1 == null || dateString2 == null) return false
    val cal1 = Calendar.getInstance().apply { time = dateString1.toDate() ?: return false }
    val cal2 = Calendar.getInstance().apply { time = dateString2.toDate() ?: return false }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

// "2024년 7월 23일 수요일" 형식으로 변환하는 함수
fun formatSeparatorDate(dateString: String?): String {
    if (dateString == null) return ""
    val date = dateString.toDate() ?: return ""
    val format = SimpleDateFormat("yyyy년 MM월 dd일 EEEE", Locale.KOREA)
    return format.format(date)
}

// "오후 2:30" 형식으로 변환하는 함수
fun formatMessageTime(dateString: String?): String {
    if (dateString == null) return ""
    val date = dateString.toDate() ?: return ""
    val format = SimpleDateFormat("a hh:mm", Locale.KOREA)
    return format.format(date)
}
