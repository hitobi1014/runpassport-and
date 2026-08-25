package com.runpassport.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.runpassport.app.ui.theme.Blue
import com.runpassport.app.ui.theme.Gray400
import com.runpassport.app.ui.theme.Gray600
import com.runpassport.app.ui.theme.Khaki
import com.runpassport.app.ui.theme.LightBlue
import com.runpassport.app.ui.theme.LightGray
import com.runpassport.app.ui.theme.LightKhaki
import com.runpassport.app.ui.theme.RunpassportTheme
import com.runpassport.app.ui.theme.Yellow

data class Course(
    val id: String,
    val name: String,
    val location: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val category: String
)

data class Badge(
    val id: String,
    val name: String,
    val isCompleted: Boolean,
    val category: String // "running" or "visit"
)

@Composable
fun HomeScreen(
    userName: String,
    weatherInfo: String,
    todayCourse: Course?,
    recommendedCourse: Course,
    recentBadges: List<Badge>,
    stampProgress: Pair<Int, Int>, // (현재, 전체)
    onCourseStartClick: (Course) -> Unit,
    onNavigateToPassport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectedTab = 0)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 인사말 & 날씨
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "안녕하세요, ${userName}님",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "오늘도 좋은 러닝되세요 달려볼까요",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                TextButton(onClick = {}) {
                    Text("또", style = MaterialTheme.typography.bodyMedium, color = Blue)
                }
            }

            // 날씨 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "☀️",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = weatherInfo,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {}) {
                    Text("미세먼지 좋음", style = MaterialTheme.typography.bodySmall, color = Blue)
                }
            }

            // 오늘의 추천 코스
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "오늘의 추천 코스",
                    style = MaterialTheme.typography.titleMedium
                )

                if (todayCourse != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "코스 사진 영역",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = CardDefaults.cardColors(containerColor = LightBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "오늘의 추천",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Blue
                                )
                                Text(
                                    text = "코스 사진 영역",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600
                                )
                            }
                        }
                    }
                }
            }

            // 추천 코스 카드
            CourseCard(
                course = recommendedCourse,
                onStartClick = { onCourseStartClick(recommendedCourse) }
            )

            // 최근 방문 지역
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "최근 방문 지역",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) { index ->
                        val backgrounds = listOf(LightKhaki, LightBlue, LightBlue)
                        val labels = listOf("제주 올레길", "경주 방문길", "강릉 안목해변")
                        RecentBadgeCard(
                            label = labels[index],
                            backgroundColor = backgrounds[index]
                        )
                    }
                }
            }

            // 나의 스탬프
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "나의 스탬프",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onNavigateToPassport) {
                        Text(
                            text = "${stampProgress.first}/${stampProgress.second} 획득",
                            style = MaterialTheme.typography.bodySmall,
                            color = Blue
                        )
                        Text(
                            text = " 패스포트 전체보기 →",
                            style = MaterialTheme.typography.bodySmall,
                            color = Blue
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentBadges) { badge ->
                        StampBadge(badge = badge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Blue, CircleShape)
                )
                Text(
                    text = course.category,
                    style = MaterialTheme.typography.labelLarge,
                    color = Blue
                )
            }

            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = course.location,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "거리 ${course.distanceKm}km",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "평균시간 ${course.estimatedMinutes}분",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "코스 시작하기",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun RecentBadgeCard(
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(backgroundColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "배지",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun StampBadge(
    badge: Badge,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = if (badge.isCompleted) {
                        if (badge.category == "running") Blue else Khaki
                    } else Gray400,
                    shape = CircleShape
                )
                .background(
                    if (badge.isCompleted) Color.White else LightGray
                ),
            contentAlignment = Alignment.Center
        ) {
            if (badge.isCompleted && badge.category == "visit") {
                Text("△", style = MaterialTheme.typography.bodyLarge, color = Khaki)
            } else if (badge.isCompleted) {
                Text("⌒", style = MaterialTheme.typography.bodyLarge, color = Blue)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "홈"
                )
            },
            label = { Text("홈", style = MaterialTheme.typography.labelLarge) },
            selected = selectedTab == 0,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "러닝") },
            label = { Text("러닝", style = MaterialTheme.typography.labelLarge) },
            selected = selectedTab == 1,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "패스포트") },
            label = { Text("패스포트", style = MaterialTheme.typography.labelLarge) },
            selected = selectedTab == 2,
            onClick = {}
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "마이") },
            label = { Text("마이", style = MaterialTheme.typography.labelLarge) },
            selected = selectedTab == 3,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    RunpassportTheme {
        HomeScreen(
            userName = "도운",
            weatherInfo = "서울 · 반포 24°C",
            todayCourse = null,
            recommendedCourse = Course(
                id = "1",
                name = "한강 반포 러닝코스",
                location = "서울 서초구",
                distanceKm = 5.2,
                estimatedMinutes = 32,
                category = "러닝코스"
            ),
            recentBadges = listOf(
                Badge("1", "서울", true, "running"),
                Badge("2", "제주", true, "visit"),
                Badge("3", "부산", true, "running"),
                Badge("4", "경주", true, "visit")
            ),
            stampProgress = 5 to 12,
            onCourseStartClick = {},
            onNavigateToPassport = {}
        )
    }
}
