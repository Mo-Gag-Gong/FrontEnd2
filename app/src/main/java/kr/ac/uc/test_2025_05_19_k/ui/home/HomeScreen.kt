package kr.ac.uc.test_2025_05_19_k.ui.home

import android.util.Log
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.ui.common.GroupCard
import kr.ac.uc.test_2025_05_19_k.ui.common.InterestTag
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
                    Log.d("HomeScreen", "스크롤 끝 감지, 다음 페이지 로드 요청.")
                    viewModel.loadNextGroupPage()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (region.isNotBlank()) "$region 지역 스터디 추천" else "스터디 추천") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateGroupClick, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "그룹 생성")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (interests.isNotEmpty()) {
                    interests.forEach { interest ->
                        InterestTag(
                            name = interest.interestName,
                            isSelected = selectedInterest == interest.interestName,
                            onClick = {
                                viewModel.onInterestClick(if (selectedInterest == interest.interestName) null else interest.interestName)
                            }
                        )
                    }
                } else if (!isLoadingInitial) {
                    Text("관심사 로딩 중이거나 없음...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoadingInitial) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (groupList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("표시할 스터디 그룹이 없습니다.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(groupList) { group ->
                        GroupCard(
                            group = group,
                            onClick = {
                                onGroupClick(group)
                            }
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
