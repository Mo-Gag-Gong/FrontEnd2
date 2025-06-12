package kr.ac.uc.test_2025_05_19_k.ui.group

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.GroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupAdminDetailViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// 탭 제목 리스트
private val adminDetailTabs = listOf("공지 사항", "멤버", "그룹 목표", "채팅", "모임")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAdminDetailScreen(
    navController: NavController,
    groupId: Long,
    viewModel: GroupAdminDetailViewModel = hiltViewModel()
) {
    val groupDetail by viewModel.groupDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // 화면이 처음 그려질 때 공지사항 목록을 불러옵니다.
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchNoticesFirstPage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupDetail?.title ?: if (isLoading) "로딩 중..." else "그룹 관리") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                adminDetailTabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = (selectedTabIndex == index),
                        onClick = { selectedTabIndex = index },
                        label = { Text(title) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.Black,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null
                    )
                }
            }

            // 선택된 탭에 따라 다른 관리 기능 화면 표시 (UI 컨텍스트 내에서 호출)
            when (selectedTabIndex) {
                0 -> AdminNoticesScreen(navController = navController, groupId = groupId, viewModel = viewModel)
                1 -> MembersTab(viewModel = viewModel, navController = navController, groupId = groupId)
                2 -> PlaceholderTab(name = "그룹 목표")
                3 -> PlaceholderTab(name = "그룹 채팅")
                4 -> PlaceholderTab(name = "그룹 모임")
            }
        }
    }
}


@Composable
fun AdminNoticesScreen(
    navController: NavController,
    groupId: Long,
    viewModel: GroupAdminDetailViewModel
) {
    val notices by viewModel.groupNotices.collectAsState()
    val isLoading by viewModel.isLoadingNotices.collectAsState()
    val showDeleteDialog by viewModel.showDeleteConfirmDialog.collectAsState()
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("공지사항 삭제") },
            text = { Text("정말로 이 공지사항을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNotice { errorMsg ->
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                    Text("취소")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && notices.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (notices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 공지사항이 없습니다.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = notices, key = { it.noticeId }) { notice ->
                    AdminNoticeCard(
                        notice = notice,
                        onEdit = { selectedNotice ->
                            val encodedTitle = URLEncoder.encode(selectedNotice.title, StandardCharsets.UTF_8.toString())
                            val encodedContent = URLEncoder.encode(selectedNotice.content, StandardCharsets.UTF_8.toString())
                            navController.navigate("notice_edit/${selectedNotice.groupId}/${selectedNotice.noticeId}?title=${encodedTitle}&content=${encodedContent}")
                        },
                        onDelete = {
                            viewModel.onOpenDeleteDialog(notice.noticeId)
                        }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                navController.navigate("notice_create/$groupId")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "공지사항 작성")
        }
    }
}

@Composable
fun AdminNoticeCard(
    notice: GroupNoticeDto,
    onEdit: (notice: GroupNoticeDto) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notice.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "작성자: ${notice.creatorName}  •  ${notice.createdAt.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(notice.content, style = MaterialTheme.typography.bodyMedium)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onEdit(notice) }) { Text("수정") }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("삭제") }
            }
        }
    }
}

@Composable
fun MembersTab(
    viewModel: GroupAdminDetailViewModel,
    navController: NavController,
    groupId: Long
) {
    val members by viewModel.groupMembers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchGroupMembers()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { navController.navigate("group_member_manage/$groupId") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("스터디 신청 목록 보기")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("멤버 목록", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(members) { member ->
                MemberCard(member = member) {
                    viewModel.kickMember(member.userId)
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: GroupMemberDto, onKick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(member.userName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "가입일: ${member.joinDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onKick) {
                Icon(Icons.Default.Cancel, contentDescription = "추방", tint = Color.Red)
            }
        }
    }
}

@Composable
fun PlaceholderTab(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name 기능은 구현 예정입니다.")
    }
}