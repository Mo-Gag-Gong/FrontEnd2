package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto

@Composable
fun MemberDetailSheetContent(
    profile: UserProfileWithStatsDto,
    showKickButton: Boolean, // 이 파라미터가 버튼 표시를 제어합니다.
    onKick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = profile.profileImage ?: "https://via.placeholder.com/150",
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(text = profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        // 'showKickButton'이 true일 때만 추방 버튼이 보이도록 하는 로직
        if (showKickButton) {
            Button(
                onClick = onKick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("추방")
            }
        }

        Text(
            text = "“${profile.name}”의 통계",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow(label = "그룹 참여 횟수", value = "${profile.groupParticipationCount}회")
            StatRow(label = "스터디 모임 출석 횟수", value = "${profile.totalMeetings}회")
            StatRow(label = "스터디 모임 출석률", value = String.format("%.0f%%", profile.attendanceRate))
        }
    }
}