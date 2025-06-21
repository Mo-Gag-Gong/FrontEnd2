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
import kr.ac.uc.test_2025_05_19_k.viewmodel.HomeViewModel

@Composable
fun ApplicationStatusDialog(
    dialogState: HomeViewModel.DialogState,
    onDismiss: () -> Unit
) {
    if (dialogState != HomeViewModel.DialogState.HIDDEN) {
        val title = when (dialogState) {
            HomeViewModel.DialogState.SUCCESS -> "신청 완료!"
            HomeViewModel.DialogState.PENDING -> "알림"
            else -> ""
        }
        val message = when (dialogState) {
            HomeViewModel.DialogState.SUCCESS -> "그룹장이 수락을 완료하면\n가입이 완료되요!"
            HomeViewModel.DialogState.PENDING -> "그룹장 수락 대기중입니다."
            else -> ""
        }

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
                    onClick = onDismiss,
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