package kr.ac.uc.test_2025_05_19_k.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.SearchViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel(),
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val recentSearches by viewModel.recentSearches.collectAsState()
    val focusManager = LocalFocusManager.current

    val performSearch = { query: String ->
        if (query.isNotBlank()) {
            viewModel.addRecentSearch(query)
            onSearch(query)
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadRecentSearches()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. 커스텀 상단 검색 바
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp), // 슬림한 디자인을 위해 패딩 조정
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("울산 근처에서 검색", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                // ▼▼▼ [수정] 검색바 배경색 및 테두리 색상 변경 ▼▼▼
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F3F3), // 회색 배경
                    unfocusedContainerColor = Color(0xFFF3F3F3)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

        // 2. 최근 검색어 영역
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ▼▼▼ [수정] '최근 검색어' 텍스트 크기 증가 ▼▼▼
                Text(
                    text = "최근 검색어",
                    style = MaterialTheme.typography.titleLarge, // 크기 키움
                    fontWeight = FontWeight.Bold
                )
                // ▼▼▼ [수정] '모두 지우기' 텍스트 색상 변경 ▼▼▼
                TextButton(onClick = { viewModel.clearAllRecentSearches() }) {
                    Text("모두 지우기", color = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (recentSearches.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("최근 검색 기록이 없습니다.", color = Color.Gray)
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentSearches.forEach { search ->
                        RecentSearchChip(
                            text = search,
                            onChipClick = {
                                searchQuery = search
                                performSearch(search)
                            },
                            onClearClick = { viewModel.removeRecentSearch(search) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 최근 검색어 칩 컴포저블
 */
@Composable
private fun RecentSearchChip(
    text: String,
    onChipClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onChipClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text)
            IconButton(onClick = onClearClick, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Close, contentDescription = "$text 검색어 삭제", tint = Color.Gray)
            }
        }
    }
}