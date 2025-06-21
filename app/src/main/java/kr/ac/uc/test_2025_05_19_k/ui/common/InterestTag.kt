package kr.ac.uc.test_2025_05_19_k.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 홈 화면에서 사용되는 관심사 태그.
 * FilterChip에서 계속되는 컴파일 오류로 인해 Surface를 사용하여 직접 구현.
 */
@Composable
fun InterestTag(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val border = if (isSelected) {
        null // 선택됐을 때는 테두리 없음
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50),
        border = border,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = name,
                color = textColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}