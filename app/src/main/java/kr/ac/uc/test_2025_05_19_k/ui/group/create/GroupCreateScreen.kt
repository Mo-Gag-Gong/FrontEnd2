package kr.ac.uc.test_2025_05_19_k.ui.group.create

import android.util.Log
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
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupCreateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreateScreen(
    navController: NavController,
    viewModel: GroupCreateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }

    // 카테고리 상태
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val interests by viewModel.interests.collectAsState()

    // 최대 멤버 수 상태
    var maxMembersExpanded by remember { mutableStateOf(false) }
    var selectedMaxMembers by remember { mutableStateOf<Int?>(null) }
    val memberCountList = (2..20).toList()

    // '완료' 버튼 활성화 여부
    val isFormValid by remember(title, selectedCategory, selectedMaxMembers) {
        derivedStateOf {
            title.isNotBlank() && selectedCategory != null && selectedMaxMembers != null
        }
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
                TextButton(
                    onClick = {
                        viewModel.createGroup(
                            title = title,
                            description = description,
                            requirements = requirements,
                            category = selectedCategory!!,
                            maxMembers = selectedMaxMembers!!,
                            onSuccess = {
                                Toast.makeText(context, "그룹이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            },
                            onError = { error ->
                                Log.e("GroupCreateScreen", "그룹 생성 실패: $error")
                                Toast.makeText(context, "그룹 생성 실패: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    enabled = isFormValid
                ) {
                    Text("완료")
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
            // 스터디 그룹 이름
            LabeledTextField(
                label = "스터디 그룹 이름",
                value = title,
                onValueChange = { title = it },
                maxLength = 20
            )

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
                            value = selectedCategory ?: "선택해주세요",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
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
                                        selectedCategory = interest.interestName
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
                            value = selectedMaxMembers?.toString() ?: "선택해주세요",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maxMembersExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
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
                                        selectedMaxMembers = count
                                        maxMembersExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 소개문
            LabeledTextField(
                label = "소개문",
                value = description,
                onValueChange = { description = it },
                maxLength = 500,
                modifier = Modifier.height(150.dp)
            )

            // 가입 요구 사항
            LabeledTextField(
                label = "가입 요구 사항",
                value = requirements,
                onValueChange = { requirements = it },
                maxLength = 500,
                modifier = Modifier.height(150.dp)
            )
        }
    }
}

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