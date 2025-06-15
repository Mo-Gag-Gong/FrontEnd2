package kr.ac.uc.test_2025_05_19_k.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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

    // ▼▼▼ [추가] BottomSheet 상태 관리를 위한 변수들 ▼▼▼
    val sheetState = rememberModalBottomSheetState()
    var selectedGroup by remember { mutableStateOf<StudyGroup?>(null) }
    val scope = rememberCoroutineScope()

    // ▼▼▼ [추가] 선택된 그룹이 있을 때만 BottomSheet를 띄웁니다 ▼▼▼
    if (selectedGroup != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedGroup = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            GroupApplySheetContent(
                group = selectedGroup!!,
                onApply = {
                    // TODO: 가입 신청 로직 연결 (ViewModel에 함수 추가 필요)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            selectedGroup = null
                        }
                    }
                }
            )
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
                                // ▼▼▼ [수정] 클릭 시 BottomSheet를 띄우도록 로직 변경 ▼▼▼
                                if (group.isMember) {
                                    // 이미 가입한 그룹은 상세 화면으로 이동
                                    navController.navigate("group_detail/${group.groupId}")
                                } else {
                                    // 가입하지 않은 그룹은 하단 시트를 표시
                                    selectedGroup = group
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}