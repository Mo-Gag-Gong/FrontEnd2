package kr.ac.uc.test_2025_05_19_k.ui.group

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kr.ac.uc.test_2025_05_19_k.model.GroupChatDto
import kr.ac.uc.test_2025_05_19_k.util.formatMessageTime
import kr.ac.uc.test_2025_05_19_k.util.formatSeparatorDate
import kr.ac.uc.test_2025_05_19_k.util.isSameDay
import kr.ac.uc.test_2025_05_19_k.viewmodel.GroupAdminDetailViewModel

@Composable
fun ChatTabScreen(viewModel: GroupAdminDetailViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val inputText by viewModel.chatInputText.collectAsState()
    val myUserId = viewModel.myUserId

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchInitialChats()
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            itemsIndexed(items = messages, key = { _, item -> item.chatId ?: "" }) { index, message ->
                val prevMessage = messages.getOrNull(index - 1)
                val showDateSeparator = !isSameDay(prevMessage?.sentAt, message.sentAt)

                if (showDateSeparator) {
                    DateSeparator(dateString = message.sentAt)
                }

                ChatItem(message = message, isMyMessage = message.senderId == myUserId)
            }
        }

        ChatInputBar(
            value = inputText,
            onValueChange = viewModel::onChatInputChanged,
            onSendClick = viewModel::sendChatMessage
        )
    }
}

@Composable
fun ChatItem(message: GroupChatDto, isMyMessage: Boolean) {
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isMyMessage) {
            TimeText(timeString = message.sentAt)
            Spacer(modifier = Modifier.width(4.dp))
        }

        Row(verticalAlignment = Alignment.Top) {
            if (!isMyMessage) {
                AsyncImage(
                    model = message.profileImage ?: "https://via.placeholder.com/150",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                if (!isMyMessage) {
                    Text(
                        text = message.userName ?: "알 수 없음",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Text(
                        text = message.message ?: "",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        if (!isMyMessage) {
            Spacer(modifier = Modifier.width(4.dp))
            TimeText(timeString = message.sentAt)
        }
    }
}

@Composable
private fun TimeText(timeString: String?) {
    Text(
        text = formatMessageTime(timeString),
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray
    )
}

// ▼▼▼ [추가] 날짜 구분선을 표시하는 Composable ▼▼▼
@Composable
private fun DateSeparator(dateString: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Gray.copy(alpha = 0.5f)
        ) {
            Text(
                text = formatSeparatorDate(dateString),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}


@Composable
fun ChatInputBar(value: String, onValueChange: (String) -> Unit, onSendClick: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("메시지...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = value.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Message")
            }
        }
    }
}