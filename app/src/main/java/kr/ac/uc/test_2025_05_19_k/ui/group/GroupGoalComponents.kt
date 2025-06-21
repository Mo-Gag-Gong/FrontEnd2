package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.util.toDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupGoalCard(
    goal: GroupGoalDto,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val startDate = toDate(goal.startDate)
    val endDate = toDate(goal.endDate)

    val status = when {
        startDate == null || endDate == null -> "날짜오류"
        today.isBefore(startDate) -> "시작 전"
        today.isAfter(endDate) -> "완료"
        else -> "진행중"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.outlinedCardColors(
            // ▼▼▼ [수정] 카드 배경색을 흰색(background)으로 변경 ▼▼▼
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ▼▼▼ [수정] 제목 텍스트 크기 증가 ▼▼▼
                Text(
                    text = goal.title ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                GoalStatusChip(status = status)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // ▼▼▼ [수정] 날짜 텍스트 크기 증가 ▼▼▼
                    Text("시작 날짜: ${goal.startDate ?: "-"}", style = MaterialTheme.typography.bodyLarge)
                    Text("종료 날짜: ${goal.endDate ?: "-"}", style = MaterialTheme.typography.bodyLarge)
                }
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "상세 보기",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun GoalStatusChip(status: String?) {
    if (status.isNullOrBlank()) return

    val (text, color) = when (status) {
        "진행중" -> "진행중" to Color(0xFFFFC107)
        "완료" -> "완료" to Color(0xFF2196F3)
        else -> "시작 전" to Color.Gray
    }
    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        contentColor = Color.White
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}