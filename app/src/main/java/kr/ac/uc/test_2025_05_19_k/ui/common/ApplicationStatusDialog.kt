package kr.ac.uc.test_2025_05_19_k.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 특정 ViewModel에 의존하지 않고, 전달된 상태에 따라 결과를 표시하는 범용 다이얼로그
 */
@Composable
fun ApplicationStatusDialog(
    applicationStatus: Pair<Boolean, String>?, // (성공 여부, 메시지)
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (applicationStatus != null) {
        val isSuccess = applicationStatus.first
        val message = applicationStatus.second
        val title = if (isSuccess) "신청 완료!" else "알림"

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("확인")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}