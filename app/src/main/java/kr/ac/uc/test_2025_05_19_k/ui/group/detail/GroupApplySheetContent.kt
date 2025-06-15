package kr.ac.uc.test_2025_05_19_k.ui.group.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup

@Composable
fun GroupApplySheetContent(
    group: StudyGroup,
    onApply: () -> Unit
) {
    // ▼▼▼ [수정] 1. 펼치기/접기 상태와 스크롤 상태를 통합 관리 ▼▼▼
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // 세로 스크롤 추가
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        // 그룹 제목
        Text(group.title, style = MaterialTheme.typography.headlineSmall)
        Text(group.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))

        // 가입 신청 버튼
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B2FF))
        ) {
            Text("가입 신청", modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))

        // ▼▼▼ [수정] 2. 상세 정보가 항상 보이도록 AnimatedVisibility 제거 ▼▼▼
        Column {
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
            Text("스터디 그룹 소개", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(group.description)
            Spacer(modifier = Modifier.height(24.dp))
            Text("위치 이름", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(group.locationName)
            Spacer(modifier = Modifier.height(24.dp))
            Text("가입 요구 사항", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(group.requirements)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ▼▼▼ [수정] 3. 하단 정보 스타일 강조 ▼▼▼
        Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Article,
                contentDescription = "관심사",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = group.interestName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.AccountCircle,
                contentDescription = "인원",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${group.currentMembers}/${group.maxMembers}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}