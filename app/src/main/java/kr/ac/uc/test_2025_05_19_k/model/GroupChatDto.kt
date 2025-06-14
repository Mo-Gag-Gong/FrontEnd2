package kr.ac.uc.test_2025_05_19_k.model

data class GroupChatDto(
    val chatId: Long?,
    val groupId: Long?,
    val senderId: Long?,
    val userName: String?,
    val profileImage: String?,
    val message: String?,
    val sentAt: String? // "yyyy-MM-dd'T'HH:mm:ss" 형식
)