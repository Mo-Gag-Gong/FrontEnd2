package kr.ac.uc.test_2025_05_19_k.ui.search

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup
import kr.ac.uc.test_2025_05_19_k.ui.common.ApplicationStatusDialog
import kr.ac.uc.test_2025_05_19_k.ui.common.HomeGroupCard
import kr.ac.uc.test_2025_05_19_k.ui.group.detail.GroupApplySheetContent
import kr.ac.uc.test_2025_05_19_k.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    navController: NavController,
    searchQuery: String,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val groupList by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val applicationStatus by viewModel.applicationStatus.collectAsState()

    var textFieldQuery by remember { mutableStateOf(searchQuery) }
    var displayedQuery by remember { mutableStateOf(searchQuery) }

    val sheetState = rememberModalBottomSheetState()
    var selectedGroup by remember { mutableStateOf<StudyGroup?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        viewModel.searchGroups(searchQuery)
    }

    // 수정된 다이얼로그 호출
    ApplicationStatusDialog(
        applicationStatus = applicationStatus,
        onDismiss = { viewModel.dismissDialog() },
        onConfirm = { viewModel.dismissDialog() }
    )

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
                        viewModel.applyToGroup(group.groupId)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SearchResultAppBar(
                query = textFieldQuery,
                onQueryChange = { textFieldQuery = it },
                onBack = { navController.popBackStack() },
                onSearch = { newQuery ->
                    if (newQuery.isNotBlank()) {
                        displayedQuery = newQuery
                        viewModel.searchGroups(newQuery)
                    }
                }
            )

            Text(
                text = "\"$displayedQuery\" 검색 결과",
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
                    unfocusedContainerColor = Color(0xFFF3F3F3),
                    focusedContainerColor = Color(0xFFF3F3F3)
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