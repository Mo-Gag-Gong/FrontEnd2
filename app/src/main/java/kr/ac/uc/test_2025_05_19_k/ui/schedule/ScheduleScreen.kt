package kr.ac.uc.test_2025_05_19_k.ui.schedule

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import kr.ac.uc.test_2025_05_19_k.viewmodel.MergedGoal
import kr.ac.uc.test_2025_05_19_k.viewmodel.PersonalGoal
import java.time.LocalDate
import java.time.YearMonth

private val SkyBlue = Color(0xFFADD8E6)
private val LightGrayText = Color(0xFF444444)

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
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text("스케줄", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SkyBlue)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SkyBlue)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "이전 달", tint = Color.White)
                }

                Text(
                    "${currentMonth.year}.${currentMonth.monthValue.toString().padStart(2, '0')}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LightGrayText
                )

                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = SkyBlue)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "다음 달", tint = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            CalendarGrid(
                month = currentMonth,
                groupDates = groupDates,
                onDateClick = { clickedDate ->
                    selectedDate = clickedDate
                    selectedGoals = goalMap[clickedDate] ?: emptyList()
                    scope.launch { sheetState.show() }
                }
            )

            Spacer(Modifier.height(24.dp))

            val mergedMap = remember(currentMonth, goalMap) {
                goalMap.filterKeys { date ->
                    date.year == currentMonth.year && date.month == currentMonth.month
                }.toSortedMap()
            }

            val mergedGoals = remember(mergedMap) {
                val flatGoals = mergedMap.flatMap { (date, goals) ->
                    goals.map { goal -> PersonalGoal(date.toString(), goal.title) }
                }
                viewModel.mergeContinuousPersonalGoals(flatGoals)
            }

            MergedGoalList(goals = mergedGoals)
        }

        if (sheetState.isVisible) {
            ModalBottomSheet(
                onDismissRequest = { scope.launch { sheetState.hide() } },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "${selectedDate} 목표",
                        color = SkyBlue,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    if (selectedGoals.isEmpty()) {
                        Text("등록된 목표 없음", color = Color.Gray)
                    } else {
                        selectedGoals.forEach { goal ->
                            Text("• ${goal.title}", color = LightGrayText, modifier = Modifier.padding(vertical = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MergedGoalList(goals: List<MergedGoal>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(goals) { goal ->
            val startDay = LocalDate.parse(goal.startDate).dayOfMonth
            val endDay = LocalDate.parse(goal.endDate).dayOfMonth

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFADD8E6)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (startDay == endDay)
                            " ${startDay}일"
                        else
                            " ${startDay}일 ~ ${endDay}일",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFADD8E6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(goal.title, color = Color.DarkGray)
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    textAlign = TextAlign.Center,
                    color = SkyBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        dates.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    val hasSchedule = date != null && groupDates.contains(date)
                    val isToday = date == today

                    val backgroundColor = when {
                        isToday -> SkyBlue.copy(alpha = 0.2f)
                        hasSchedule -> SkyBlue.copy(alpha = 0.1f)
                        else -> Color.Transparent
                    }

                    val textColor = when {
                        isToday -> SkyBlue
                        else -> Color.DarkGray
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .clickable(enabled = date != null) {
                                date?.let { onDateClick(it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = textColor,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
