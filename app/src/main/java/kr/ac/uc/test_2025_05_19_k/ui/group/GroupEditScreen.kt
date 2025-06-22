// app/src/main/java/kr/ac/uc/test_2025_05_19_k/ui/group/GroupEditScreen.kt

package kr.ac.uc.test_2025_05_19_k.ui.group

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupEditEvent
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditScreen(
    navController: NavController,
    viewModel: GroupEditViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val interests by viewModel.interests.collectAsState()
    val isUpdating by viewModel.isUpdating.collectAsState()
    val showDisbandDialog by viewModel.showDisbandDialog.collectAsState()

    var categoryExpanded by remember { mutableStateOf(false) }
    var maxMembersExpanded by remember { mutableStateOf(false) }
    val memberCountList = (2..20).toList()

    // 유효성 검사
    val isFormValid by remember(
        viewModel.title,
        viewModel.selectedInterestName,
        viewModel.maxMembers
    ) {
        derivedStateOf {
            viewModel.title.isNotBlank() &&
                    viewModel.selectedInterestName != null &&
                    viewModel.maxMembers.isNotBlank()
        }
    }

    // ViewModel의 이벤트를 구독하여 처리
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is GroupEditEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is GroupEditEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // 그룹 해산 확인 다이얼로그
    if (showDisbandDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDisbandDialogDismiss() },
            title = { Text("그룹 해산") },
            text = { Text("정말로 그룹을 해산하시겠습니까? 이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDisbandGroup() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("해산") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDisbandDialogDismiss() }) { Text("취소") }
            }
        )
    }

    Scaffold(
        containerColor = Color.White,
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
                TextButton(
                    onClick = {
                        viewModel.updateGroupInfo(
                            onSuccess = {
                            },
                            onError = { errorMessage ->
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = isFormValid && !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("완료", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 스터디 그룹 이름
            LabeledTextField(
                label = "스터디 그룹 이름",
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                maxLength = 20
            )
            Spacer(Modifier.height(24.dp))

            // 카테고리 & 최대 멤버 수
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("카테고리", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedInterestName ?: "선택해주세요",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF3F3F3),
                                unfocusedContainerColor = Color(0xFFF3F3F3),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            interests.forEach { interest ->
                                DropdownMenuItem(
                                    text = { Text(interest.interestName) },
                                    onClick = {
                                        viewModel.selectedInterestName = interest.interestName
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("최대 멤버 수", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = maxMembersExpanded,
                        onExpandedChange = { maxMembersExpanded = !maxMembersExpanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.maxMembers.ifEmpty { "선택해주세요" },
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maxMembersExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF3F3F3),
                                unfocusedContainerColor = Color(0xFFF3F3F3),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = maxMembersExpanded,
                            onDismissRequest = { maxMembersExpanded = false }
                        ) {
                            memberCountList.forEach { count ->
                                DropdownMenuItem(
                                    text = { Text("$count 명") },
                                    onClick = {
                                        viewModel.maxMembers = count.toString()
                                        maxMembersExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // 소개문
            LabeledTextField(
                label = "소개문",
                value = viewModel.description,
                onValueChange = { viewModel.description = it },
                maxLength = 500,
                modifier = Modifier.height(150.dp)
            )
            Spacer(Modifier.height(24.dp))

            // 가입 요구 사항
            LabeledTextField(
                label = "가입 요구 사항",
                value = viewModel.requirements,
                onValueChange = { viewModel.requirements = it },
                maxLength = 500,
                modifier = Modifier.height(150.dp)
            )

            Spacer(Modifier.weight(1f)) // 버튼을 하단에 위치시키기 위한 Spacer

            // 그룹 해산 버튼
            Button(
                onClick = { viewModel.onDisbandButtonClick() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
            ) {
                Text("그룹 해산", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

// 재사용 가능한 텍스트 필드
@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = { if (it.length <= maxLength) onValueChange(it) },
            modifier = modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF3F3F3),
                unfocusedContainerColor = Color(0xFFF3F3F3)
            ),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(text = "내용을 입력해주세요.", color = Color.Gray) },
            supportingText = {
                Text(
                    text = "${value.length} / $maxLength",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
    }
}