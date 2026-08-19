package com.runpassport.app

import javax.inject.Inject

// Hilt DI 그래프 동작 확인용 임시 클래스 — MainActivity 화면에서 값이 보이면 지워도 됨.
class HiltSmokeTestRepository @Inject constructor() {
    fun greet(): String = "Hilt 연결됨"
}
