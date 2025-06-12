package kr.ac.uc.test_2025_05_19_k.ui.group

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupMemberDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMemberDetailScreen(
    navController: NavController,
    viewModel: GroupMemberDetailViewModel = hiltViewModel()
) {
    val memberInfo by viewModel.memberInfo.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(memberInfo?.name ?: "멤버 정보") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        memberInfo?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                AsyncImage(
                    model = info.profileImage ?: "https://via.placeholder.com/150",
                    contentDescription = "멤버 프로필 사진",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(info.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Text("통계 정보", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                StatisticsCard(info)

                Spacer(modifier = Modifier.weight(1f))

                // status 값에 따라 올바른 버튼만 표시되도록 when 문으로 통합
                when (viewModel.status) {
                    "ACTIVE" -> {
                        // 기존 멤버일 경우 '추방하기' 버튼만 표시
                        Button(
                            onClick = {
                                viewModel.kickMember { success ->
                                    if (success) {
                                        Toast.makeText(context, "${info.name}님을 추방했습니다.", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(context, "추방에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("그룹에서 추방하기")
                        }
                    }
                    "PENDING" -> {
                        // 신청자일 경우 '거절', '승인' 버튼만 표시
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.rejectMember { success ->
                                        if (success) {
                                            Toast.makeText(context, "${info.name}님의 신청을 거절했습니다.", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(context, "거절에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("거절")
                            }
                            Button(
                                onClick = {
                                    viewModel.approveMember { success ->
                                        if (success) {
                                            Toast.makeText(context, "${info.name}님의 신청을 승인했습니다.", Toast.LENGTH_SHORT).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(context, "승인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("승인")
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun StatisticsCard(info: UserProfileWithStatsDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatItem("그룹 참여 횟수", "${info.groupParticipationCount}회")
            StatItem("스터디 출석률", "${String.format("%.1f", info.attendanceRate)}%")
            StatItem("총 모임 횟수", "${info.totalMeetings}회")
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}