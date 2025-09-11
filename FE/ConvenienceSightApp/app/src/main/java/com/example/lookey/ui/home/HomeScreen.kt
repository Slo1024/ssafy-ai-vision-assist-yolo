// ui/home/HomeScreen.kt
package com.example.lookey.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lookey.R
import com.example.lookey.core.platform.tts.TtsController
import com.example.lookey.ui.components.BigActionButton

@Composable
fun HomeScreen(
    tts: TtsController,
    onAllergy: () -> Unit = {},
    onFindStore: () -> Unit = {},
    onFindProduct: () -> Unit = {},
    onCart: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        tts.speak("LooKey 홈입니다. 알레르기, 편의점 찾기, 상품 찾기, 장바구니 버튼이 있습니다.")
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.lookey),
                contentDescription = "LooKey 로고",
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("LooKey", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        BigActionButton("알레르기", onClick = onAllergy)
        BigActionButton("편의점 찾기", onClick = onFindStore)
        BigActionButton("상품 찾기", onClick = onFindProduct)
        BigActionButton("장바구니", onClick = onCart)

        Spacer(Modifier.weight(1f))
    }
}
