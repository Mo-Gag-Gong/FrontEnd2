package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kr.ac.uc.test_2025_05_19_k.model.GroupMemberDto
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupMemberManageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupMemberManageScreen(
    navController: NavController,
    viewModel: GroupMemberManageViewModel = hiltViewModel()
) {
    val pendingMembers by viewModel.pendingMembers.collectAsState()
    val selectedMemberProfile by viewModel.selectedMemberProfile.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner.lifecycle) {
        lifecycleOwner.lifecycle.currentStateFlow
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
                    viewModel.fetchPendingMembers()
                }
            }
    }

    if (selectedMemberProfile != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedMember() },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            // ▼▼▼ [수정] showKickButton 파라미터에 false 전달 ▼▼▼
            MemberDetailSheetContent(
                profile = selectedMemberProfile!!,
                showKickButton = false, // 신청 목록에서는 추방 버튼을 보여주지 않음
                onKick = {}
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스터디 신청 목록") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (pendingMembers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("대기중인 신청자가 없습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(pendingMembers, key = { _, item -> item.membershipId }) { index, member ->
                    PendingMemberListItem(
                        member = member,
                        onViewProfileClick = { viewModel.onMemberSelected(member.userId) },
                        onApproveClick = { viewModel.approveMember(member.userId) },
                        onRejectClick = { viewModel.rejectMember(member.userId) }
                    )
                    if (index < pendingMembers.lastIndex) {
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingMemberListItem(
    member: GroupMemberDto,
    onViewProfileClick: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onViewProfileClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = member.profileImage ?: "https://via.placeholder.com/150",
                contentDescription = "Applicant Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(member.userName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onApproveClick) {
                Text("승인")
            }
            Button(
                onClick = onRejectClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("거부")
            }
        }
    }
}