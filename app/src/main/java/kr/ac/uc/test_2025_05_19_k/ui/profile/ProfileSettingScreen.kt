package kr.ac.uc.test_2025_05_19_k.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kr.ac.uc.test_2025_05_19_k.R
import kr.ac.uc.test_2025_05_19_k.viewmodel.ProfileInputViewModel

@Composable
fun SignInProfileSettingScreen(
    navController: NavController,
    viewModel: ProfileInputViewModel = hiltViewModel(),
    onPrev: () -> Unit = {},
    onNext: (name: String, gender: String, phone: String, birth: Int) -> Unit = { _, _, _, _ -> }
) {
    val name = viewModel.name
    val gender = viewModel.gender
    val phoneNumber = viewModel.phoneNumber
    val birthYear = viewModel.birthYear
    val birthMonth = viewModel.birthMonth
    val birthDay = viewModel.birthDay

    val isNameValid = name.isNotBlank()
    val isGenderValid = gender.isNotBlank()
    val isPhoneValid = phoneNumber.replace("-", "").matches(Regex("^01[0-9]{8,9}$"))
    val isBirthValid = try {
        java.time.LocalDate.of(birthYear.toInt(), birthMonth.toInt(), birthDay.toInt())
        true // ✅ 성공 시 Boolean true 반환
    } catch (e: Exception) {
        false
    }


    val isFormValid = isNameValid && isGenderValid && isPhoneValid && isBirthValid

    val textFieldColors = TextFieldDefaults.colors(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        focusedContainerColor = Color(0xFFF1F1F1),
        unfocusedContainerColor = Color(0xFFF1F1F1)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 바
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Button(
                onClick = {
                    if (isFormValid) {
                        val birth = "${birthYear.padStart(4, '0')}${birthMonth.padStart(2, '0')}${birthDay.padStart(2, '0')}".toInt()
                        onNext(name, gender, phoneNumber, birth)
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormValid) Color.Cyan else Color.LightGray
                )
            ) {
                Text("다음", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("모각공에 오신 것을 환영합니다!", fontWeight = FontWeight.Bold)
        Text("이제부터 당신에 대해 알려주세요!", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // 캐릭터 로고
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.log),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 이름
        InputLabel("이름")
        TextField(
            value = name,
            onValueChange = { viewModel.updateName(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("이름을 입력하세요") },
            colors = textFieldColors,
            singleLine = true
        )
        if (!isNameValid && name.isNotEmpty()) {
            Text("이름을 입력하세요.", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 성별
        InputLabel("성별")
        Row(modifier = Modifier.fillMaxWidth()) {
            GenderButton("남", gender == "남") { viewModel.updateGender("남") }
            Spacer(modifier = Modifier.width(8.dp))
            GenderButton("여", gender == "여") { viewModel.updateGender("여") }
        }
        if (!isGenderValid) {
            Text("성별을 선택하세요.", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 전화번호
        InputLabel("전화번호")
        TextField(
            value = phoneNumber,
            onValueChange = { viewModel.updatePhoneNumber(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("전화번호 입력") },
            colors = textFieldColors,
            singleLine = true
        )
        if (phoneNumber.isNotBlank() && !isPhoneValid) {
            Text("올바른 전화번호 형식(01012345678 또는 010-1234-5678)을 입력하세요.", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 생년월일
        InputLabel("생년월일")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ✅ 년도 입력란
            TextField(
                value = birthYear,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }
                    if (digits.length <= 4) viewModel.updateBirthYearPart(digits)  // ← 이 부분!
                },
                placeholder = { Text("년") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                singleLine = true
            )

            // 월 입력란
            TextField(
                value = birthMonth,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }
                    if (digits.length <= 2) viewModel.updateBirthMonthPart(digits)
                },
                placeholder = { Text("월") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                singleLine = true
            )

            // 일 입력란
            TextField(
                value = birthDay,
                onValueChange = {
                    val digits = it.filter { c -> c.isDigit() }
                    if (digits.length <= 2) viewModel.updateBirthDayPart(digits)
                },
                placeholder = { Text("일") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                singleLine = true
            )
        }


        if (!isBirthValid && birthYear.isNotBlank() && birthMonth.isNotBlank() && birthDay.isNotBlank()) {
            Text("올바른 생년월일(YYYY-MM-DD)을 입력하세요.", color = Color.Red, fontSize = 12.sp)
        }
    }
}

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun RowScope.GenderButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.Cyan else Color.LightGray
        )
    ) {
        Text(label, color = if (selected) Color.White else Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileInputScreen() {
    val navController = androidx.navigation.compose.rememberNavController()
    SignInProfileSettingScreen(navController = navController)
}
