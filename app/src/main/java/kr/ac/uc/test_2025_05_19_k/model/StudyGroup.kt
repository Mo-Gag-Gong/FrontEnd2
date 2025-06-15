package kr.ac.uc.test_2025_05_19_k.model

data class StudyGroup( // 또는 StudyGroupDto
    val groupId: Long, // id -> groupId
    val creatorId: Long, // 추가
    val creatorName: String, // 추가
    val title: String, // name -> title
    val interestName: String, // category -> interestName
    val description: String,
    val locationName: String, // 추가
    val startDate: String?, // 추가 (날짜 타입은 String 또는 적절한 Date 타입으로)
    val endDate: String?, // 추가
    val maxMembers: Int,
    val currentMembers: Int, // memberCount -> currentMembers
    val requirements: String, // 추가
    val isActive: Boolean, // 추가
    val createdAt: String, // 추가 (날짜시간 타입은 String 또는 적절한 DateTime 타입으로)
    val updatedAt: String, // 추가
    val isMember: Boolean
)
