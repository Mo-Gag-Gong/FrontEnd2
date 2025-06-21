package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kr.ac.uc.test_2025_05_19_k.model.GoalDetailDto
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.ui.group.GoalStatusChip
import kr.ac.uc.test_2025_05_19_k.util.toDate
import java.time.LocalDate

@Composable
fun GoalDetailSheetContent(
    goal: GroupGoalDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleDetail: (Long) -> Unit,
    isReadOnly: Boolean = false
) {
    val scrollState = rememberScrollState()

    val today = LocalDate.now()
    val startDate = toDate(goal.startDate)
    val endDate = toDate(goal.endDate)

    val status = when {
        startDate == null || endDate == null -> "날짜오류"
        today.isBefore(startDate) -> "시작 전"
        today.isAfter(endDate) -> "완료"
        else -> "진행중"
    }

    // ▼▼▼ [수정] isExpanded 상태 및 관련 로직 제거, Column 구조 변경 ▼▼▼
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
            .verticalScroll(scrollState)
    ) {
        // 목표 제목
        Text(
            text = goal.title,
            style = MaterialTheme.typography.headlineSmall, // 크기 증가
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 작성자 및 날짜 정보
        Text(
            "작성자: ${goal.creatorName}",
            style = MaterialTheme.typography.bodyLarge, // 크기 증가
            color = Color.Gray
        )
        Text(
            "시작 날짜: ${goal.startDate}",
            style = MaterialTheme.typography.bodyLarge, // 크기 증가
            color = Color.Gray
        )
        Text(
            "종료 날짜: ${goal.endDate}",
            style = MaterialTheme.typography.bodyLarge, // 크기 증가
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 세부 목표 목록
        goal.details.forEach { detail ->
            DetailItem(
                detail = detail,
                enabled = !isReadOnly,
                onToggle = {
                    detail.detailId?.let { id -> onToggleDetail(id) }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!isReadOnly) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onEditClick, modifier = Modifier.weight(1f)) { Text("수정") }
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("삭제") }
                GoalStatusChip(status = status)
            }
        }
    }
}

@Composable
private fun DetailItem(
    detail: GoalDetailDto,
    enabled: Boolean, // enabled 파라미터 추가
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ▼▼▼ [수정] 세부 목표 텍스트 크기 증가 ▼▼▼
        Text(
            text = detail.description ?: "",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = detail.isCompleted,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00B2FF)
            )
        )
    }
}