package kr.ac.uc.test_2025_05_19_k.ui.group.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kr.ac.uc.test_2025_05_19_k.util.toDate


@Composable
fun GoalItem(
    goal: GroupGoalDto,
    onClick: () -> Unit
) {
    // ▼▼▼ [추가] 날짜를 비교하여 동적으로 상태 결정 ▼▼▼
    val today = Date()
    val startDate = goal.startDate?.toDate()
    val endDate = goal.endDate?.toDate()

    val status = when {
        startDate == null || endDate == null -> "날짜오류"
        today.before(startDate) -> "시작 전"
        today.after(endDate) -> "완료"
        else -> "진행중"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = goal.title ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "시작: ${goal.startDate ?: "-"} / 종료: ${goal.endDate ?: "-"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                // ▼▼▼ [수정] 서버 status 대신 위에서 계산한 status 전달 ▼▼▼
                GoalStatusChip(status = status)
            }
        }
    }
}

@Composable
fun GoalStatusChip(status: String?) { // ▼▼▼ [수정] 타입을 String? 으로 변경 ▼▼▼
    if (status.isNullOrBlank()) return // status가 null이면 아무것도 하지 않고 함수 종료

    val (text, color) = when (status) {
        "진행중" -> "진행중" to Color(0xFF4CAF50)
        "완료" -> "완료" to Color.Gray
        else -> "시작 전" to Color(0xFF2196F3)
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        contentColor = Color.White
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}