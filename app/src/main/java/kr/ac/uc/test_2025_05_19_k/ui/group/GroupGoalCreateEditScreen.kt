package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val isEditMode = uiState.isEditMode

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("삭제 확인") },
            text = { Text("정말로 이 목표를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: ViewModel에 목표 삭제 함수 연결 필요
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditMode) {
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Text("삭제", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(
                        onClick = {
                            // ▼▼▼ [수정] 저장 성공 시, 이전 화면에 새로고침 신호를 보냄 ▼▼▼
                            viewModel.saveGoal {
                                // 1. 이전 화면의 StateHandle에 "should_refresh_goals"를 true로 설정
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("should_refresh_goals", true)
                                // 2. 현재 화면을 닫음
                                navController.popBackStack()
                            }
                        },
                        enabled = uiState.isFormValid
                    ) {
                        Text(if (isEditMode) "수정" else "완료")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ▼▼▼ [수정] 모드에 따라 화면 제목 변경 ▼▼▼
            Text(
                text = if (isEditMode) "공지 사항 수정" else "공지 사항 작성",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            // 목표 제목 입력 필드
            Column {
                Text("목표 제목", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = uiState.title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("목표 제목을 입력해주세요", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF3F3F3),
                        unfocusedContainerColor = Color(0xFFF3F3F3)
                    ),
                    singleLine = true
                )
            }

            // 날짜 선택 필드
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("시작 날짜", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    DateField(date = uiState.startDate, onClick = { showStartDatePicker = true })
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("종료 날짜", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    DateField(date = uiState.endDate, onClick = { showEndDatePicker = true })
                }
            }

            // 세부 목표 설정
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("세부 목표 설정", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = viewModel::addDetailField, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "세부 목표 추가", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.details.forEachIndexed { index, text ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = text,
                                onValueChange = { viewModel.onDetailChange(index, it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("세부 목표 ${index + 1}", color = Color.Gray) },
                                shape = RoundedCornerShape(12.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color(0xFFF3F3F3),
                                    unfocusedContainerColor = Color(0xFFF3F3F3)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.removeDetailField(index) }) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = "필드 제거", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

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
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.onEndDateChange(it.toFormattedDate())
                        }
                        showEndDatePicker = false
                    }
                ) { Text("확인") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun DateField(date: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3F3F3)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = if (date.isEmpty()) "YYYY.MM.DD" else date, color = if (date.isEmpty()) Color.Gray else Color.Black)
            Icon(Icons.Default.CalendarMonth, contentDescription = "날짜 선택", tint = Color.Gray)
        }
    }
}

private fun Long.toFormattedDate(): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = this
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(calendar.time)
}