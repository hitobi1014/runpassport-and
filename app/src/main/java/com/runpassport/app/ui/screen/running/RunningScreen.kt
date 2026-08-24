package com.runpassport.app.ui.screen.running

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.runpassport.app.ui.theme.Blue
import com.runpassport.app.ui.theme.DarkGray
import com.runpassport.app.ui.theme.Gray600
import com.runpassport.app.ui.theme.RunpassportTheme

data class TrackingState(
    val courseName: String,
    val progressPercent: Float,
    val distanceKm: Double,
    val timeElapsed: String, // "MM:SS" 형식
    val currentPaceSeconds: Int, // 초 단위
    val isRunning: Boolean
)

@Composable
fun RunningScreen(
    trackingState: TrackingState,
    onClose: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 닫기 버튼 & 코스명
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.White
                    )
                }
                Text(
                    text = trackingState.courseName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp)) // 대칭을 위한 여백
            }

            // 중앙: 맵 영역
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF2A3F4F)),
                    contentAlignment = Alignment.Center
                ) {
                    // 실제 맵은 나중에 Google Maps로 교체
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "코스 지도 영역",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                        // 임시 트래킹 라인 그리기
                        Canvas(
                            modifier = Modifier
                                .size(200.dp, 100.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(50f, size.height - 50f)
                                cubicTo(
                                    100f, size.height - 100f,
                                    150f, size.height - 150f,
                                    200f, 100f
                                )
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1976D2),
                                style = Stroke(width = 8f)
                            )
                            // 시작점
                            drawCircle(
                                color = Color.White,
                                radius = 12f,
                                center = Offset(50f, size.height - 50f)
                            )
                            // 현재 위치
                            drawCircle(
                                color = Color(0xFF1976D2),
                                radius = 12f,
                                center = Offset(200f, 100f)
                            )
                            // 목표 지점
                            drawCircle(
                                color = Color.White,
                                radius = 12f,
                                center = Offset(size.width - 50f, 100f),
                                style = Stroke(width = 4f)
                            )
                        }
                    }
                }

                // 진행률 바
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "출발",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray600
                        )
                        Text(
                            text = "${(trackingState.progressPercent * 100).toInt()}% 완주",
                            style = MaterialTheme.typography.bodySmall,
                            color = Blue
                        )
                        Text(
                            text = "도착",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray600
                        )
                    }
                    LinearProgressIndicator(
                        progress = { trackingState.progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Blue,
                        trackColor = Gray600
                    )
                }
            }

            // 하단: 통계 & 컨트롤
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 거리
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "거리",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format("%.2f", trackingState.distanceKm),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                        Text(
                            text = "km",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // 시간 & 평균 페이스
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "시간",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Text(
                            text = trackingState.timeElapsed,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "평균 페이스",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Text(
                            text = "${trackingState.currentPaceSeconds / 60}'${trackingState.currentPaceSeconds % 60}\"",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                // 컨트롤 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    FloatingActionButton(
                        onClick = onPauseResume,
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        containerColor = Color(0xFF3E3E3E),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Text(
                            text = if (trackingState.isRunning) "❚❚" else "▶",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    FloatingActionButton(
                        onClick = onStop,
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        containerColor = Blue,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Text(
                            text = "■",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RunningScreenPreview() {
    RunpassportTheme {
        RunningScreen(
            trackingState = TrackingState(
                courseName = "한강 반포 러닝코스",
                progressPercent = 0.62f,
                distanceKm = 3.24,
                timeElapsed = "18:42",
                currentPaceSeconds = 347, // 5'47"
                isRunning = true
            ),
            onClose = {},
            onPauseResume = {},
            onStop = {}
        )
    }
}
