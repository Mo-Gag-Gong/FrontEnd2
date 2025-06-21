// 파일 위치: repository/PersonalScheduleStorage.kt
package kr.ac.uc.test_2025_05_19_k.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kr.ac.uc.test_2025_05_19_k.viewmodel.GoalResponse
import java.time.LocalDate

class PersonalScheduleStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("personal_schedule", Context.MODE_PRIVATE)

    private val gson = Gson()

    // ✅ 저장
    fun savePersonalGoals(goalMap: Map<LocalDate, List<GoalResponse>>) {
        val stringKeyMap = goalMap.mapKeys { it.key.toString() } // LocalDate -> String
        val json = gson.toJson(stringKeyMap)
        prefs.edit().putString("goal_map", json).apply()
    }

    // ✅ 불러오기
    fun loadPersonalGoals(): Map<LocalDate, List<GoalResponse>> {
        val json = prefs.getString("goal_map", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, List<GoalResponse>>>() {}.type
        val stringKeyMap: Map<String, List<GoalResponse>> = gson.fromJson(json, type)
        return stringKeyMap.mapKeys { LocalDate.parse(it.key) }
    }

    // ✅ 삭제
    fun clear() {
        prefs.edit().remove("goal_map").apply()
    }
}
