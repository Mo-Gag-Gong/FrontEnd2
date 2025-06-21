package kr.ac.uc.test_2025_05_19_k.ui.group.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.GroupGoalDto
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.GroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.ui.group.ChatTabScreen
import kr.ac.uc.test_2025_05_19_k.ui.group.GoalStatusChip
import kr.ac.uc.test_2025_05_19_k.ui.group.MemberDetailSheetContent
import kr.ac.uc.test_2025_05_19_k.util.toDate
import kr.ac.uc.test_2025_05_19_k.viewmodel.JoinedGroupDetailViewModel
import java.time.LocalDate
import kr.ac.uc.test_2025_05_19_k.ui.group.GroupGoalCard
import kr.ac.uc.test_2025_05_19_k.ui.group.GoalDetailSheetContent


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JoinedGroupDetailScreen(
    navController: NavController,
    viewModel: JoinedGroupDetailViewModel = hiltViewModel()
) {
    val groupTitle by viewModel.groupTitle.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val members by viewModel.members.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedMemberProfile by viewModel.selectedMemberProfile.collectAsState()

    // ▼▼▼ [추가] 탈퇴 확인 다이얼로그의 표시 여부를 관리하는 상태 ▼▼▼
    var showLeaveConfirmDialog by remember { mutableStateOf(false) }

    val goalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedGoalDetail by viewModel.selectedGoalDetail.collectAsState()


    val tabs = listOf("공지사항", "멤버", "그룹 목표", "채팅", "모임")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    if (selectedGoalDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedGoal() },
            sheetState = goalSheetState
        ) {
            GoalDetailSheetContent(
                goal = selectedGoalDetail!!,
                onEditClick = {},
                onDeleteClick = {},
                onToggleDetail = {},
                isReadOnly = true // 참여자는 읽기 전용
            )
        }
    }

    // ▼▼▼ [추가] 탈퇴 확인 다이얼로그 UI ▼▼▼
    if (showLeaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveConfirmDialog = false },
            title = { Text("그룹 탈퇴") },
            text = { Text("정말로 이 그룹을 탈퇴하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.leaveGroup() // ViewModel의 탈퇴 함수 호출
                        showLeaveConfirmDialog = false
                    }
                ) {
                    Text("탈퇴", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveConfirmDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (selectedMemberProfile != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedMember() },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            MemberDetailSheetContent(
                profile = selectedMemberProfile!!,
                showKickButton = false,
                onKick = {}
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.leaveGroupEvent.collectLatest {
            navController.navigateUp()
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            when (pagerState.currentPage) {
                0 -> viewModel.loadNotices()
                1 -> viewModel.loadMembers()
                2 -> viewModel.loadGoals()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = (pagerState.currentPage == index),
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
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

            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> NoticesTabContent(notices = notices)
                    1 -> MembersTabContent(
                        members = members,
                        currentUserId = currentUserId,
                        // ▼▼▼ [수정] 탈퇴 버튼 클릭 시, 바로 함수를 호출하는 대신 다이얼로그를 띄우도록 변경 ▼▼▼
                        onLeaveClick = { showLeaveConfirmDialog = true },
                        onMemberClick = { memberId ->
                            viewModel.onMemberSelected(memberId)
                        }
                    )
                    2 -> GoalsTabContent(
                        goals = goals,
                        onGoalClick = { goalId ->
                            viewModel.onGoalSelected(goalId)
                        }
                    )
                    3 -> ChatTabScreen(navController = navController, groupId = viewModel.groupId)
                    4 -> PlaceholderContent(text = "모임 기능은 준비 중입니다.")
                }
            }
        }
    }
}

// 공지사항 탭 컨텐츠... (이전과 동일)
@Composable
fun NoticesTabContent(notices: List<GroupNoticeDto>) {
    if (notices.isEmpty()) {
        PlaceholderContent(text = "등록된 공지사항이 없습니다.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            itemsIndexed(items = notices, key = { _, it -> it.noticeId }) { index, notice ->
                ParticipantNoticeItem(notice = notice)
                if (index < notices.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun ParticipantNoticeItem(notice: GroupNoticeDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(notice.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(notice.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "작성자",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = notice.creatorName,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun GoalsTabContent(
    goals: List<GroupGoalDto>,
    onGoalClick: (Long) -> Unit
) {
    if (goals.isEmpty()) {
        PlaceholderContent(text = "등록된 그룹 목표가 없습니다.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = goals, key = { it.goalId }) { goal ->
                // 관리자 화면과 동일한 GroupGoalCard 재사용
                GroupGoalCard(goal = goal) {
                    onGoalClick(goal.goalId)
                }
            }
        }
    }
}

@Composable
fun ParticipantGoalItem(goal: GroupGoalDto, onClick: () -> Unit) {
    // 날짜를 기준으로 상태를 동적으로 계산
    val today = LocalDate.now()
    val startDate = toDate(goal.startDate)
    val endDate = toDate(goal.endDate)
    val status = when {
        startDate == null || endDate == null -> "날짜오류"
        today.isBefore(startDate) -> "시작 전"
        today.isAfter(endDate) -> "완료"
        else -> "진행중"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // 연한 회색 배경
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 상단 행: 제목과 상태 칩
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                GoalStatusChip(status = status)
            }
            // 하단: 날짜 정보
            Column {
                Text(
                    text = "시작 날짜: ${goal.startDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = "종료 날짜: ${goal.endDate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ParticipantNoticeCard(notice: GroupNoticeDto) {
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
        }
    }
}


// 멤버 탭 컨텐츠
@Composable
fun MembersTabContent(
    members: List<GroupMemberDto>,
    currentUserId: Long?,
    onLeaveClick: () -> Unit,
    onMemberClick: (Long) -> Unit
) {
    if (members.isEmpty()) {
        PlaceholderContent(text = "멤버 정보를 불러오는 중입니다...")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(items = members, key = { _, member -> member.userId }) { index, member ->
                MemberListItem(
                    member = member,
                    isCurrentUser = member.userId == currentUserId,
                    onLeaveClick = onLeaveClick,
                    onMemberClick = { onMemberClick(member.userId) }
                )
                if (index < members.lastIndex) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
private fun MemberListItem(
    member: GroupMemberDto,
    isCurrentUser: Boolean,
    onLeaveClick: () -> Unit,
    onMemberClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMemberClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.profileImage ?: "https://via.placeholder.com/150",
            contentDescription = "멤버 프로필 사진",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = if (isCurrentUser) "${member.userName} (나)" else member.userName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(1f))

        if (isCurrentUser) {
            TextButton(onClick = onLeaveClick) {
                Text("탈퇴", color = MaterialTheme.colorScheme.error)
            }
        } else {
            Text(
                text = "가입일 ${member.joinDate}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


// 임시 컨텐츠 ... (이전과 동일)
@Composable
fun PlaceholderContent(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}