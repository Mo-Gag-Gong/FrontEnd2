package kr.ac.uc.test_2025_05_19_k.ui.search

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.ui.common.HomeGroupCard
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.GroupApplySheetContent
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    navController: NavController,
    searchQuery: String,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val groupList by viewModel.groupList.collectAsState()
    val isLoading by viewModel.isLoadingInitial.collectAsState()

    // ▼▼▼ [수정] 1. 상태 변수 분리 ▼▼▼
    // 검색 '입력창'의 텍스트를 위한 상태
    var textFieldQuery by remember { mutableStateOf(searchQuery) }
    // 화면 '제목'에 표시될, 실제 검색이 실행된 검색어를 위한 상태
    var displayedQuery by remember { mutableStateOf(searchQuery) }


    val sheetState = rememberModalBottomSheetState()
    var selectedGroup by remember { mutableStateOf<StudyGroup?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        viewModel.fetchSearchResults(searchQuery)
    }

    if (selectedGroup != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedGroup = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            GroupApplySheetContent(
                group = selectedGroup!!,
                onApply = {
                    scope.launch {
                        try {
                            viewModel.applyToGroup(selectedGroup!!.groupId)
                            Toast.makeText(context, "가입 신청이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "가입 신청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            sheetState.hide()
                            selectedGroup = null
                        }
                    }
                }
            )
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SearchResultAppBar(
                query = textFieldQuery, // 입력창은 textFieldQuery를 사용
                onQueryChange = { textFieldQuery = it }, // 입력 시 textFieldQuery만 변경
                onBack = { navController.popBackStack() },
                onSearch = { newQuery ->
                    // ▼▼▼ [수정] 2. 실제 검색 시에만 제목(displayedQuery)을 업데이트 ▼▼▼
                    if (newQuery.isNotBlank()) {
                        displayedQuery = newQuery
                        viewModel.fetchSearchResults(newQuery)
                    }
                }
            )

            // ▼▼▼ [수정] 3. 제목 텍스트가 displayedQuery를 보도록 변경 ▼▼▼
            Text(
                text = "\"${displayedQuery}\" 검색 결과",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (groupList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(
                        items = groupList,
                        key = { group -> group.groupId }
                    ) { group ->
                        HomeGroupCard(
                            group = group,
                            onClick = {
                                if (group.isMember) {
                                    navController.navigate("group_detail/${group.groupId}")
                                } else {
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

/**
 * 검색 결과 화면의 상단 앱 바
 */
@Composable
private fun SearchResultAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearch: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("검색어 입력", color = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF3F3F3),
                    unfocusedContainerColor = Color(0xFFF3F3F3)
                ),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "검색어 지우기")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(query)
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    }
}