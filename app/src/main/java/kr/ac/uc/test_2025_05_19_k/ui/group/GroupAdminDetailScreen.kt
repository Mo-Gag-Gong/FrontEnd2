package kr.ac.uc.test_2025_05_19_k.ui.group

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.model.GroupNoticeDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupAdminDetailViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val context = LocalContext.current

    val memberSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedMemberProfile by viewModel.selectedMemberProfile.collectAsState()
    val scope = rememberCoroutineScope()

    if (selectedMemberProfile != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedMember() },
            sheetState = memberSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            // ▼▼▼ [수정] showKickButton 파라미터에 올바른 조건 전달 ▼▼▼
            val isCurrentUserCreator = viewModel.myUserId == groupDetail?.creatorId
            MemberDetailSheetContent(
                profile = selectedMemberProfile!!,
                showKickButton = isCurrentUserCreator && !selectedMemberProfile!!.isOwnProfile,
                onKick = {
                    viewModel.kickMember(selectedMemberProfile!!.userId) {
                        scope.launch {
                            Toast.makeText(context, "${selectedMemberProfile!!.name}님을 추방했습니다.", Toast.LENGTH_SHORT).show()
                            viewModel.clearSelectedMember()
                        }
                    }
                }
            )
        }
    }


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
                    // ▼▼▼ [수정] 탭 버튼 색상 변경 ▼▼▼
                    FilterChip(
                        selected = (selectedTabIndex == index),
                        onClick = { viewModel.onTabSelected(index) },
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

            when (selectedTabIndex) {
                0 -> AdminNoticesScreen(navController = navController, groupId = groupId, viewModel = viewModel)
                1 -> MembersTab(viewModel = viewModel, navController = navController, groupId = groupId)
                2 -> AdminGoalsScreen(navController = navController, groupId = groupId, viewModel = viewModel)
                3 -> ChatTabScreen(navController = navController, groupId = groupId)
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(items = notices, key = { _, it -> it.noticeId }) { index, notice ->
                    AdminNoticeCard(
                        notice = notice,
                        onEditClick = {
                            val encodedTitle = URLEncoder.encode(notice.title, StandardCharsets.UTF_8.toString())
                            val encodedContent = URLEncoder.encode(notice.content, StandardCharsets.UTF_8.toString())
                            navController.navigate("notice_edit/${notice.groupId}/${notice.noticeId}?title=${encodedTitle}&content=${encodedContent}")
                        }
                    )
                    if (index < notices.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }

        // ▼▼▼ [수정] 글쓰기 버튼 디자인 변경 ▼▼▼
        ExtendedFloatingActionButton(
            onClick = {
                navController.navigate("notice_create/$groupId")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            text = { Text("글쓰기", fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Filled.Add, contentDescription = "공지사항 작성") },
            containerColor = Color(0xFF00B2FF),
            contentColor = Color.White
        )
    }
}

@Composable
fun AdminNoticeCard(
    notice: GroupNoticeDto,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ▼▼▼ [수정] 제목 텍스트 스타일 변경 ▼▼▼
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, fill = false) // 버튼에 공간을 주기 위해
            )
            // ▼▼▼ [추가] 수정 버튼 아이콘 ▼▼▼
            IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "공지 수정", tint = Color.Gray)
            }
        }

        // ▼▼▼ [수정] 내용 텍스트 스타일 변경 ▼▼▼
        Text(
            text = notice.content,
            style = MaterialTheme.typography.bodyLarge, // 크기 키움
            lineHeight = 24.sp
        )
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersTab(
    viewModel: GroupAdminDetailViewModel,
    navController: NavController,
    groupId: Long
) {
    val members by viewModel.groupMembers.collectAsState()
    val groupDetail by viewModel.groupDetail.collectAsState()
    val hasPending by viewModel.hasPendingMembers.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchGroupMembers()
        viewModel.checkPendingMembers()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (members.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("멤버가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(items = members, key = { _, item -> item.userId }) { index, member ->
                    MemberListItem(
                        member = member,
                        isCreator = member.userId == groupDetail?.creatorId,
                        onClick = {
                            viewModel.onMemberSelected(member.userId)
                        }
                    )
                    if (index < members.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }
        }

        BadgedBox(
            badge = {
                if (hasPending) {
                    Badge(
                        containerColor = Color.Red,
                        modifier = Modifier.offset(x = (-10).dp, y = 10.dp)
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("group_member_manage/$groupId") },
                shape = CircleShape,
                containerColor = Color(0xFF00B2FF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "스터디 신청 목록 보기", tint = Color.White)
            }
        }
    }
}

@Composable
private fun MemberListItem(
    member: GroupMemberDto,
    isCreator: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = member.profileImage ?: "https://via.placeholder.com/150",
            contentDescription = "${member.userName} profile picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = if (isCreator) "(그룹장) ${member.userName}" else member.userName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCreator) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "가입일 ${member.joinDate}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun PlaceholderTab(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$name 기능은 구현 예정입니다.")
    }
}