package com.example.lookey.ui.dev

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lookey.ui.components.BannerMessage
import com.example.lookey.ui.components.ConfirmModal
import com.example.lookey.ui.scan.ResultFormatter

@Composable
fun DevComponentsScreen() {
    var banner by remember { mutableStateOf<ResultFormatter.Banner?>(null) }
    var showModal by remember { mutableStateOf(false) }
    var target by remember { mutableStateOf("코카콜라 제로 500ml") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // 상단에 실제 배너/모달을 Scan과 같은 방식으로 띄우기
        banner?.let { b ->
            Box(Modifier.align(Alignment.TopCenter)) {
                BannerMessage(
                    banner = b,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                )
            }
        }

        if (showModal) {
            Box(Modifier.align(Alignment.TopCenter)) {
                ConfirmModal(
                    text = "\"$target\" 장바구니에 있습니다. 이걸로 안내할까요?",
                    yesText = "예",
                    noText = "아니요",
                    onYes = {
                        showModal = false
                        banner = ResultFormatter.Banner(
                            type = ResultFormatter.Banner.Type.SUCCESS,
                            text = "[$target] 안내를 시작합니다."
                        )
                    },
                    onNo = { showModal = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                )
            }
        }

        // 하단 제어 버튼들
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    banner = ResultFormatter.Banner(
                        type = ResultFormatter.Banner.Type.INFO,
                        text = "코카콜라 제로 500ml | 2,200원\n1+1 행사품입니다."
                    )
                }) { Text("배너(INFO)") }

                Button(onClick = {
                    banner = ResultFormatter.Banner(
                        type = ResultFormatter.Banner.Type.WARNING,
                        text = "우유 200ml | 1,200원\n주의: 유당 포함"
                    )
                }) { Text("배너(WARN)") }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(onClick = { showModal = true }) {
                Text("장바구니 모달 열기")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(onClick = { banner = null; showModal = false }) {
                Text("모두 닫기")
            }
        }
    }
}
