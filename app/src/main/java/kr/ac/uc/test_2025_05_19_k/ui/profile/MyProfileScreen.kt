// app/src/main/java/kr/ac/uc/test_2025_05_19_k/ui/profile/MyProfileScreen.kt
package kr.ac.uc.test_2025_05_19_k.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kr.ac.uc.test_2025_05_19_k.viewmodel.ProfileInputViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.UserProfileViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val profileState = viewModel.userProfile.value

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    if (profileState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val profile = profileState

    Column(modifier = Modifier.padding(16.dp)) {
        Text("마이 프로필", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation()
        ) {
            Box {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(profile.profileImage),
                            contentDescription = "프로필",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Black, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))

                        val formattedBirth = profile.birthYear?.toString()?.let {
                            if (it.length == 8) {
                                val year = it.substring(0, 4)
                                val month = it.substring(4, 6)
                                val day = it.substring(6, 8)
                                "${year}년 ${month}월 ${day}일"
                            } else "미입력"
                        } ?: "미입력"

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text("성별: ${profile.gender ?: "미입력"}")
                            Text("전화번호: ${profile.phoneNumber ?: "미입력"}")
                            Text("생년월일: $formattedBirth")
                        }
                    }
                }

                IconButton(
                    onClick = { navController.navigate("profile_edit") },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "정보 수정")
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 관심사 제목 + 수정 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "“${profile.name}”의 관심사",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f) // 텍스트 너비 제한
                    )

                    IconButton(
                        onClick = { navController.navigate("interest_edit") }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "관심사 수정")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 관심사 버튼 목록
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.interests.forEach {
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                        ) {
                            Text(it.interestName)
                        }
                    }
                }
            }

        }


        // 기존 관심사 Box 이후에 추가 (통계 Box 이전)
        Spacer(modifier = Modifier.height(16.dp))

        val context = LocalContext.current
        val displayedLocation = remember {
            UserPreference(context).getLocation() ?: "지역 정보 없음"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                .padding(16.dp)
                .clickable { navController.navigate("region_setting_cache") }
        ) {
            Column {
                Text("현재 지역", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayedLocation,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "터치하여 지역 정보 수정",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }




        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("통계", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("그룹 참여 횟수: ${profile.groupParticipationCount}회")
                Text("총 모임 수: ${profile.totalMeetings}회")
                Text("출석률: ${profile.attendanceRate}%")
            }
        }
    }
}
@Composable
fun ProfileEditScreen(
    navController: NavController,
    viewModel: ProfileInputViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf("") }
    var birthYearOnly by remember { mutableStateOf("") }
    var birthMonthOnly by remember { mutableStateOf("") }
    var birthDayOnly by remember { mutableStateOf("") }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            name = it.name
            gender = it.gender
            phoneNumber = it.phoneNumber ?: ""
            it.birthYear?.let { birthInt ->
                val birthStr = birthInt.toString().padStart(8, '0')
                birthYearOnly = birthStr.substring(0, 4)
                birthMonthOnly = birthStr.substring(4, 6)
                birthDayOnly = birthStr.substring(6, 8)
            }
        }
    }

    if (userProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("마이 프로필 수정", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Image(
            painter = rememberAsyncImagePainter(userProfile?.profileImage ?: ""),
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenderButton("남", gender == "남") { gender = "남" }
            GenderButton("여", gender == "여") { gender = "여" }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("전화번호") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text("생년월일", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = birthYearOnly,
                onValueChange = { birthYearOnly = it },
                label = { Text("년") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = birthMonthOnly,
                onValueChange = { birthMonthOnly = it },
                label = { Text("월") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = birthDayOnly,
                onValueChange = { birthDayOnly = it },
                label = { Text("일") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val birthInt = try {
                (birthYearOnly + birthMonthOnly.padStart(2, '0') + birthDayOnly.padStart(2, '0')).toInt()
            } catch (e: Exception) {
                Toast.makeText(context, "생년월일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                return@Button
            }

            Log.d("SubmitDebug", "버튼 클릭됨")
            Log.d("SubmitDebug", "name=$name")
            Log.d("SubmitDebug", "gender=$gender")
            Log.d("SubmitDebug", "phoneNumber=$phoneNumber")
            Log.d("SubmitDebug", "birthInt=$birthInt")
            Log.d("SubmitDebug", "locationName=${viewModel.locationName}") // ✅ 위치 로그 출력

            viewModel.submitBasicProfileOnly(
                name = name,
                gender = gender ?: "",
                phoneNumber = phoneNumber,
                birthYear = birthInt,
                locationName = viewModel.locationName, // ✅ 위치 포함
                onSuccess = {
                    Toast.makeText(context, "수정 완료!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onError = { msg ->
                    Toast.makeText(context, "오류: $msg", Toast.LENGTH_LONG).show()
                }
            )
        }) {
            Text("저장", color = Color.White, fontSize = 18.sp)
        }
    }
}





@Composable
fun GenderButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.Cyan else Color.LightGray
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(label, color = if (selected) Color.White else Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun MyProfileScreenPreview() {
    MyProfileScreen(navController = NavController(LocalContext.current) as NavHostController)
}
