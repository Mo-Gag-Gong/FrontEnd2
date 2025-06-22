package kr.ac.uc.test_2025_05_19_k.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.ui.common.ApplicationStatusDialog
import kr.ac.uc.test_2025_05_19_k.ui.common.HomeGroupCard
import kr.ac.uc.test_2025_05_19_k.ui.common.InterestTag
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.GroupApplySheetContent
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val region by viewModel.region.collectAsState()
    val interests by viewModel.interests.collectAsState()
    val selectedInterest by viewModel.selectedInterest.collectAsState()
    val groupList by viewModel.groupList.collectAsState()
    val isLoadingInitial by viewModel.isLoadingInitial.collectAsState()

    // ▼▼▼ [수정] ViewModel의 applicationStatus 상태를 구독 ▼▼▼
    val applicationStatus by viewModel.applicationStatus.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var selectedGroup by remember { mutableStateOf<StudyGroup?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) } // BottomSheet 표시 여부 상태
    val scope = rememberCoroutineScope()

    // ▼▼▼ [수정] 새로운 ApplicationStatusDialog 호출 방식 ▼▼▼
    ApplicationStatusDialog(
        applicationStatus = applicationStatus,
        onDismiss = { viewModel.dismissDialog() },
        onConfirm = { viewModel.dismissDialog() }
    )

    // BottomSheet 표시 로직
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            selectedGroup?.let { group ->
                GroupApplySheetContent(
                    group = group,
                    onApply = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                        viewModel.applyToGroup(group.groupId)
                    }
                )
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("group_create") },
                containerColor = Color(0xFF00B2FF),
                contentColor = Color.White,
                text = { Text("만들기") },
                icon = { Icon(Icons.Default.Add, contentDescription = "그룹 생성") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 커스텀 상단바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (region.isNotBlank()) region else "지역 정보 없음",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { navController.navigate("search") }) {
                    Icon(Icons.Default.Search, contentDescription = "검색", modifier = Modifier.size(28.dp))
                }
            }

            // 관심사 태그
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InterestTag(
                    name = "전체",
                    isSelected = selectedInterest == null,
                    onClick = { viewModel.onInterestClick(null) }
                )
                interests.forEach { interest ->
                    InterestTag(
                        name = interest.interestName,
                        isSelected = selectedInterest == interest.interestName,
                        onClick = { viewModel.onInterestClick(interest.interestName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 그룹 목록
            if (isLoadingInitial) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupList, key = { it.groupId }) { group ->
                        HomeGroupCard(
                            group = group,
                            onClick = {
                                if (group.isMember) {
                                    navController.navigate("group_detail/${group.groupId}")
                                } else {
                                    // 가입하지 않은 그룹은 하단 시트를 표시
                                    selectedGroup = group
                                    showBottomSheet = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}