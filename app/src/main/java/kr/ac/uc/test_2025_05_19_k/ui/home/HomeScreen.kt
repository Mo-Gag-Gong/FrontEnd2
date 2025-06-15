package kr.ac.uc.test_2025_05_19_k.ui.home

import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.ui.common.HomeGroupCard
import kr.ac.uc.test_2025_05_19_k.ui.common.InterestTag
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel
import androidx.compose.ui.graphics.Color

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onGroupClick: (StudyGroup) -> Unit,
    onCreateGroupClick: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    val region by viewModel.region.collectAsState()
    val interests by viewModel.interests.collectAsState()
    val selectedInterest by viewModel.selectedInterest.collectAsState()
    val groupList by viewModel.groupList.collectAsState()
    val isLoadingInitial by viewModel.isLoadingInitial.collectAsState()
    val isLoadingNextPage by viewModel.isLoadingNextPage.collectAsState()
    val isLastPage by viewModel.isLastPage.collectAsState()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState, isLoadingNextPage, isLastPage, groupList) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItemIndex ->
                val totalItemsCount = lazyListState.layoutInfo.totalItemsCount
                if (groupList.isNotEmpty() && lastVisibleItemIndex != null &&
                    lastVisibleItemIndex >= totalItemsCount - 3 &&
                    !isLoadingInitial && !isLoadingNextPage && !isLastPage
                ) {
                    viewModel.loadNextGroupPage()
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateGroupClick,
                containerColor = Color(0xFF00B2FF),
                contentColor = Color.White,
                text = { Text("만들기", fontWeight = FontWeight.Bold) },
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
                IconButton(onClick = onNavigateToSearch) {
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
            } else if (groupList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("표시할 스터디 그룹이 없습니다.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupList, key = { it.groupId }) { group ->
                        HomeGroupCard(
                            group = group,
                            onClick = { onGroupClick(group) }
                        )
                    }
                    if (isLoadingNextPage) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}