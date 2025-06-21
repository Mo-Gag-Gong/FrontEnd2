package kr.ac.uc.test_2025_05_19_k.ui.schedule

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.viewmodel.GoalResponse
import kr.ac.uc.test_2025_05_19_k.viewmodel.GoalViewModel
import java.time.LocalDate
import java.time.YearMonth

enum class ScheduleType {
    NONE, GROUP
}

fun getScheduleTypeForDate(
    date: LocalDate,
    groupDates: Set<LocalDate>
): ScheduleType {
    val hasGroup = groupDates.contains(date)
    return if (hasGroup) ScheduleType.GROUP else ScheduleType.NONE
}

fun getColorForScheduleType(type: ScheduleType): Color {
    return when (type) {
        ScheduleType.NONE -> Color.Transparent
        ScheduleType.GROUP -> Color(0xFFFFCDD2)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(groupId: Long, navController: NavController) {
    val viewModel: GoalViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val goalMap by viewModel.goalMap.collectAsState()
    val groupDates = goalMap.keys.toSet()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGoals by remember { mutableStateOf<List<GoalResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        viewModel.loadGoalsFromMyGroups()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("\uD83D\uDCC5 스케줄") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "이전 달")
                }
                Text(
                    "${currentMonth.year}.${currentMonth.monthValue.toString().padStart(2, '0')}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "다음 달")
                }
            }

            Spacer(Modifier.height(8.dp))

            CalendarGrid(
                month = currentMonth,
                groupDates = groupDates
            ) { clickedDate ->
                selectedDate = clickedDate
                selectedGoals = goalMap[clickedDate] ?: emptyList()
                scope.launch { sheetState.show() }
            }

            Spacer(Modifier.height(16.dp))

            val mergedMap = remember(currentMonth, goalMap) {
                val merged = mutableMapOf<LocalDate, MutableList<GoalResponse>>()
                goalMap.forEach { (date, list) ->
                    if (date.year == currentMonth.year && date.month == currentMonth.month) {
                        merged.getOrPut(date) { mutableListOf() }.addAll(list)
                    }
                }
                merged.toSortedMap()
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                mergedMap.forEach { (date, goals) ->
                    item {
                        // ✅ 날짜 전체 출력 대신 '일'만 출력 (예: 22일)
                        Text("\uD83D\uDCC5 ${date.dayOfMonth}일", fontWeight = FontWeight.Bold)
                    }
                    items(goals.size) { index ->
                        val goal = goals[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(goal.title)
                            }
                        }
                    }
                }
            }

        }

        if (sheetState.isVisible) {
            ModalBottomSheet(
                onDismissRequest = { scope.launch { sheetState.hide() } },
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("\uD83D\uDCCC ${selectedDate} 목표", style = MaterialTheme.typography.titleMedium)

                    if (selectedGoals.isEmpty()) {
                        Text("등록된 목표 없음")
                    } else {
                        selectedGoals.forEach { goal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("• ${goal.title}", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    month: YearMonth,
    groupDates: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDay = month.atDay(1)
    val lastDay = month.lengthOfMonth()
    val firstWeekday = (firstDay.dayOfWeek.value % 7)

    val totalCells = ((firstWeekday + lastDay + 6) / 7) * 7
    val dates = (0 until totalCells).map { index ->
        val day = index - firstWeekday + 1
        if (day in 1..lastDay) month.atDay(day) else null
    }

    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        dates.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val type = if (date != null)
                        getScheduleTypeForDate(date, groupDates)
                    else ScheduleType.NONE

                    val bgColor = getColorForScheduleType(type)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable(enabled = date != null) {
                                date?.let { onDateClick(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = if (date == today) Color.Black else Color.DarkGray,
                            fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
