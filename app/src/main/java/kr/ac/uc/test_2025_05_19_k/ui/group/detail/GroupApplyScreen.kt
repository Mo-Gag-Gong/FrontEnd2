// app/src/main/java/kr/ac/uc/test_2025_05_19_k/ui/group/detail/GroupApplyScreen.kt
package kr.ac.uc.test_2025_05_19_k.ui.group.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.StudyGroupDetail
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupApplyScreen(
    navController: NavController,
    groupId: Long,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val groupDetail by viewModel.groupDetail.collectAsState()
    val detailErrorMessage by viewModel.detailErrorMessage.collectAsState()
    val applySuccess by viewModel.applySuccess.collectAsState()

    // 공지사항 및 그룹장 관련 상태 구독 제거
    // val groupNotices by viewModel.groupNotices.collectAsState()
    // val isLoadingNotices by viewModel.isLoadingNotices.collectAsState()
    // val isCurrentUserCreator by viewModel.isCurrentUserCreator.collectAsState()


    applySuccess?.let {
        Toast.makeText(context, if (it) "가입 신청 완료" else "가입 신청 실패 또는 이미 처리됨", Toast.LENGTH_SHORT).show()
        viewModel.resetApplyStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupDetail?.title ?: if (isLoadingDetail) "로딩 중..." else "그룹 정보") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
        // FAB (공지사항 작성 버튼) 제거
        // floatingActionButton = { ... }
    ) { paddingValues ->
        when {
            isLoadingDetail -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            detailErrorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = detailErrorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
            groupDetail == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("해당 그룹 정보를 찾을 수 없습니다.", textAlign = TextAlign.Center)
                }
            }
            else -> {
                val detail = groupDetail!!
                // GroupDetailContent를 원래의 간단한 버전으로 되돌리거나, 내부에서 공지사항 관련 UI 제거
                OriginalGroupDetailContent( // 함수 이름을 변경하여 구분 (또는 기존 함수 내용 수정)
                    modifier = Modifier.padding(paddingValues),
                    detail = detail,
                    onApplyToGroup = { viewModel.applyToGroup() }
                )
            }
        }
    }
}

// 기존 그룹 상세 정보만 표시하는 Composable (공지사항 부분 없음)
@Composable
fun OriginalGroupDetailContent(
    modifier: Modifier = Modifier,
    detail: StudyGroupDetail,
    onApplyToGroup: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(detail.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 4.dp))
        Text(detail.locationName, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("스터디 그룹 소개", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
                Text(text = detail.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("가입 요구 사항", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 4.dp))
                Text(text = detail.requirements, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("관심사: ${detail.interestName}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("인원: ${detail.currentMembers}/${detail.maxMembers}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (detail.alreadyApplied) {
            Text(
                text = "가입 신청 완료",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(bottom = 16.dp)
            )
        } else {
            // 가입 신청 버튼의 enabled 조건에서 isCurrentUserCreator 제거
            Button(
                onClick = onApplyToGroup,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                enabled = detail.currentMembers < detail.maxMembers // 그룹장이 자기 그룹에 신청하는 경우는 없다고 가정
            ) {
                Text("가입 신청")
            }
        }
    }
}