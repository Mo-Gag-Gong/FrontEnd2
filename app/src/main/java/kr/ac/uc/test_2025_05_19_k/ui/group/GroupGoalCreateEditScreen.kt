package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupGoalCreateEditViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupGoalCreateEditScreen(
    navController: NavController,
    viewModel: GroupGoalCreateEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "목표 수정" else "목표 생성") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveGoal {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("should_refresh_goals", true)
                            navController.popBackStack()
                        }
                    }) {
                        Text("완료")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("목표 제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- 날짜 입력 Row ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 시작 날짜 TextField
                    OutlinedTextField(
                        value = uiState.startDate,
                        onValueChange = {},
                        label = { Text("시작 날짜") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            showStartDatePicker = true
                                        }
                                    }
                                }
                            }
                    )
                    // 종료 날짜 TextField
                    OutlinedTextField(
                        value = uiState.endDate,
                        onValueChange = {},
                        label = { Text("종료 날짜") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        interactionSource = remember { MutableInteractionSource() }
                            .also { interactionSource ->
                                LaunchedEffect(interactionSource) {
                                    interactionSource.interactions.collect {
                                        if (it is PressInteraction.Release) {
                                            showEndDatePicker = true
                                        }
                                    }
                                }
                            }
                    )
                }

                // ▼▼▼ [추가] 경고 상태가 true일 때만 Text를 표시 ▼▼▼
                if (uiState.showEndDateWarning) {
                    Text(
                        text = "경고: 종료 날짜는 현재 날짜보다 이전일 수 없습니다.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("세부 목표 설정", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- 세부 목표 리스트 ---
            items(uiState.details.size) { index ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = uiState.details[index],
                        onValueChange = { viewModel.onDetailChange(index, it) },
                        label = { Text("세부 목표 ${index + 1}") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.removeDetailField(index) }) {
                        Icon(Icons.Default.Remove, contentDescription = "필드 제거")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- 세부 목표 추가 버튼 ---
            item {
                Button(
                    onClick = viewModel::addDetailField,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("세부 목표 추가")
                }
            }
        }
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.wrapContentSize(Alignment.Center))
        }
    }

    // --- 다이얼로그 로직 ---
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onStartDateChange(it.toFormattedDate())
                        }
                        showStartDatePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            // ▼▼▼ [수정] onEndDateChange -> onEndDateChanged로 호출 함수 변경 ▼▼▼
                            // 이전 답변에서 onEndDateChanged로 제안했었으므로, ViewModel의 함수 이름과 일치시킵니다.
                            viewModel.onEndDateChange(it.toFormattedDate())
                        }
                        showEndDatePicker = false
                    }
                ) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun Long.toFormattedDate(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(calendar.time)
}