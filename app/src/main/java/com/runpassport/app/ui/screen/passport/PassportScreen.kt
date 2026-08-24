package com.runpassport.app.ui.screen.passport

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.runpassport.app.ui.theme.Blue
import com.runpassport.app.ui.theme.Gray400
import com.runpassport.app.ui.theme.Khaki
import com.runpassport.app.ui.theme.LightGray
import com.runpassport.app.ui.theme.LightKhaki
import com.runpassport.app.ui.theme.RunpassportTheme

data class StampItem(
    val id: String,
    val name: String,
    val date: String?, // "YY.MM.DD" 형식, null이면 미획득
    val category: String, // "running" 또는 "visit"
    val isCompleted: Boolean
)

@Composable
fun PassportScreen(
    stamps: List<StampItem>,
    progress: Pair<Int, Int>, // (현재, 전체)
    selectedFilter: Int, // 0: 전체, 1: 수도권, 2: 강원, 3: 경상, 4: 전라·제주
    onFilterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단: 제목 & 진행률
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "나의 러닝 패스포트",
                style = MaterialTheme.typography.headlineLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(Blue, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.weight(0.1f))
                Text(
                    text = "${progress.first} / ${progress.second} 지역 완료",
                    style = MaterialTheme.typography.bodySmall,
                    color = Blue
                )
            }
        }

        // 탭: 전체 / 수도권 / 강원 / 경상 / 전라·제주
        TabRow(
            selectedTabIndex = selectedFilter,
            containerColor = Color.White,
            contentColor = Blue
        ) {
            listOf("전체", "수도권", "강원", "경상", "전라·제주").forEachIndexed { index, title ->
                Tab(
                    selected = selectedFilter == index,
                    onClick = { onFilterSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                )
            }
        }

        // 스탬프 그리드
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightKhaki)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(stamps) { stamp ->
                    StampCard(stamp = stamp)
                }
            }
        }
    }
}

@Composable
fun StampCard(
    stamp: StampItem,
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
                .clip(CircleShape)
                .border(
                    width = 3.dp,
                    color = when {
                        !stamp.isCompleted -> Gray400
                        stamp.category == "running" -> Blue
                        else -> Khaki
                    },
                    shape = CircleShape
                )
                .background(
                    if (stamp.isCompleted) Color.White else LightGray
                ),
            contentAlignment = Alignment.Center
        ) {
            if (stamp.isCompleted) {
                if (stamp.category == "running") {
                    // 러닝 스탬프: 곡선 모양
                    Text(
                        text = "⌒",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Blue
                    )
                } else {
                    // 방문 스탬프: 삼각형 모양
                    Text(
                        text = "△",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Khaki
                    )
                }
            } else {
                // 미획득 스탬프
                Text(
                    text = if (stamp.category == "running") "⌒" else "△",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Gray400
                )
            }
        }

        Text(
            text = stamp.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (stamp.isCompleted) Color.Black else Gray400
        )

        Text(
            text = stamp.date ?: "미도전",
            style = MaterialTheme.typography.bodySmall,
            color = if (stamp.isCompleted) Color.Black else Gray400
        )
    }
}

@Composable
fun StampLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Blue)
            )
            Text(
                text = "강원 · 러닝 코스",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Khaki)
            )
            Text(
                text = "속리산 · 올레길 코스",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PassportScreenPreview() {
    RunpassportTheme {
        PassportScreen(
            stamps = listOf(
                StampItem("1", "서울 관악", "26.03.02", "running", true),
                StampItem("2", "부산 해운대", "26.04.18", "running", true),
                StampItem("3", "강릉 안목", "26.03.08", "running", true),
                StampItem("4", "속리산 길찾", "26.05.22", "visit", true),
                StampItem("5", "제주 올레길", "26.06.14", "visit", true),
                StampItem("6", "경주 탐험길", null, "visit", false),
                StampItem("7", "전주 한옥마을", null, "visit", false),
                StampItem("8", "여수 해안로", null, "running", false)
            ),
            progress = 5 to 12,
            selectedFilter = 0,
            onFilterSelected = {}
        )
    }
}
