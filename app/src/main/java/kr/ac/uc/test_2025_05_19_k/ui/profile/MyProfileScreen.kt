package kr.ac.uc.test_2025_05_19_k.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kr.ac.uc.test_2025_05_19_k.viewmodel.ProfileInputViewModel
import kr.ac.uc.test_2025_05_19_k.viewmodel.UserProfileViewModel
import androidx.compose.foundation.layout.FlowRow
import kr.ac.uc.test_2025_05_19_k.data.local.UserPreference

val LightBlue = Color(0xFFADD8E6)
val LightGrayBackground = Color(0xFFF5F5F5)

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
    val context = LocalContext.current
    val displayedLocation = remember {
        UserPreference(context).getLocation() ?: "지역 정보 없음"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text("마이 프로필", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
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
                                .border(2.dp, LightBlue, CircleShape),
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
                            Text(profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("성별: ${profile.gender ?: "미입력"}", color = Color.Gray)
                            Text("전화번호: ${profile.phoneNumber ?: "미입력"}", color = Color.Gray)
                            Text("생년월일: $formattedBirth", color = Color.Gray)
                        }
                    }
                }

                IconButton(
                    onClick = { navController.navigate("profile_edit") },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "정보 수정", tint = LightBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "“${profile.name}”의 관심사",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { navController.navigate("interest_edit") }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "관심사 수정", tint = LightBlue)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.interests.forEach {
                        Button(
                            onClick = {},
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LightBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text(it.interestName)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightGrayBackground, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .clickable { navController.navigate("region_setting_cache") }
        ) {
            Column {
                Text("📍 현재 지역", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(displayedLocation, fontSize = 14.sp, color = Color.DarkGray)
                Text("터치하여 지역 정보 수정", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📊 통계", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("그룹 참여 횟수: ${profile.groupParticipationCount}회", color = Color.Gray)
                Text("총 모임 수: ${profile.totalMeetings}회", color = Color.Gray)
                Text("출석률: ${profile.attendanceRate}%", color = Color.Gray)
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
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("마이 프로필 수정", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(16.dp))

        Image(
            painter = rememberAsyncImagePainter(userProfile?.profileImage ?: ""),
            contentDescription = "프로필 이미지",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(2.dp, LightBlue, CircleShape)
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

        Text("생년월일", fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

        Button(
            onClick = {
                val birthInt = try {
                    (birthYearOnly + birthMonthOnly.padStart(2, '0') + birthDayOnly.padStart(2, '0')).toInt()
                } catch (e: Exception) {
                    Toast.makeText(context, "생년월일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                viewModel.submitBasicProfileOnly(
                    name = name,
                    gender = gender ?: "",
                    phoneNumber = phoneNumber,
                    birthYear = birthInt,
                    locationName = viewModel.locationName,
                    onSuccess = {
                        Toast.makeText(context, "수정 완료!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    onError = { msg ->
                        Toast.makeText(context, "오류: $msg", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LightBlue,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Text("저장", fontSize = 18.sp)
        }
    }
}


@Composable
fun GenderButton(label: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) LightBlue else Color.White,
            contentColor = if (selected) Color.White else LightBlue
        ),
        border = if (!selected) BorderStroke(1.dp, LightBlue) else null
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun MyProfileScreenPreview() {
    MyProfileScreen(navController = NavController(LocalContext.current) as NavHostController)
}
