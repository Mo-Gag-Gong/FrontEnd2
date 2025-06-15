package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kr.ac.uc.test_2025_05_19_k.model.UserProfileWithStatsDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupMemberDetailViewModel
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMemberDetailScreen(
    navController: NavController,
    viewModel: GroupMemberDetailViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
    val context = LocalContext.current
    val loggedInUserId by viewModel.loggedInUserId.collectAsState()

    // [추가] ViewModel의 이벤트를 처리하는 부분
    LaunchedEffect(key1 = Unit) {
        viewModel.actionResultEvent.collectLatest { result ->
            Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            if (result.isSuccess) {
                // 성공 시 이전 화면으로 돌아감
                navController.popBackStack()
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.name ?: "멤버 프로필") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            userProfile?.let { profile ->
                ProfileHeader(profile)
                StatisticsCard(profile)

                if (isOwner) {
                    when (viewModel.status) {
                        "ACTIVE" -> {
                            if (loggedInUserId != profile.userId) {
                                DangerButton(
                                    onClick = { viewModel.kickMember() },
                                    text = "추방하기"
                                )
                            }
                        }
                        "PENDING" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SecondaryButton(
                                    onClick = { viewModel.rejectMember() },
                                    text = "거절",
                                    modifier = Modifier.weight(1f)
                                )
                                PrimaryButton(
                                    onClick = { viewModel.approveMember() },
                                    text = "승인",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// ... ProfileHeader, StatisticsCard, Button Composable 등 나머지 코드는 동일 ...

@Composable
fun ProfileHeader(profile: UserProfileWithStatsDto) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = profile.profileImage ?: "https://via.placeholder.com/150",
            contentDescription = "사용자 프로필 이미지",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Text(text = profile.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatisticsCard(profile: UserProfileWithStatsDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("통계 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            StatRow("그룹 참여 횟수", "${profile.groupParticipationCount}회")
            StatRow("스터디 출석률", String.format("%.1f%%", profile.attendanceRate))
            StatRow("총 모임 횟수", "${profile.totalMeetings}회")
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun PrimaryButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier) {
        Text(text)
    }
}

@Composable
fun SecondaryButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Text(text)
    }
}

@Composable
fun DangerButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = modifier
    ) {
        Text(text)
    }
}