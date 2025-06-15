package kr.ac.uc.test_2025_05_19_k.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.ac.uc.test_2025_05_19_k.model.StudyGroup

@Composable
fun HomeGroupCard(
    group: StudyGroup,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 18.sp
            )
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ▼▼▼ [수정] 아이콘과 텍스트의 크기 및 색상 변경 ▼▼▼
                Icon(
                    imageVector = Icons.Outlined.Article,
                    contentDescription = "관심사",
                    modifier = Modifier.size(18.dp), // 크기 증가
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // 색상 진하게
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = group.interestName,
                    fontSize = 13.sp, // 크기 증가
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // 색상 진하게
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "인원",
                    modifier = Modifier.size(18.dp), // 크기 증가
                    tint = MaterialTheme.colorScheme.onSurfaceVariant // 색상 진하게
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = group.currentMembers.toString(),
                    fontSize = 13.sp, // 크기 증가
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // 색상 진하게
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}