// app/src/main/java/com/example/lookey/ui/scan/ResultFormatter.kt
package com.example.lookey.ui.scan

import com.example.lookey.domain.entity.DetectResult
import java.text.NumberFormat
import java.util.Locale

object ResultFormatter {

    data class Banner(val type: Type, val text: String) {
        enum class Type { WARNING, INFO, SUCCESS }
    }
    data class Voice(val text: String)

    private fun formatPrice(price: Int?): String =
        if (price == null) "" else NumberFormat.getNumberInstance(Locale.KOREA).format(price)

    /**
     * 배너: 상품 정보 한 번에
     * 예)
     *  먹태깡 청양마요 맛 | 1,700원
     *  2+1 행사품입니다.
     *  (알레르기 시)
     *  주의: 유당 포함
     */
    fun toBanner(r: DetectResult): Banner {
        val head = buildString {
            append(r.name)
            r.price?.let { append(" | ${formatPrice(it)}원") }
        }

        val lines = mutableListOf(head)

        // 행사 문구
        r.promo?.let { lines += "${it} 행사품입니다." }

        // 주의 성분(있을 경우 경고 배너로 승격)
        val hasWarn = r.hasAllergy
        if (hasWarn) {
            lines += "주의: ${r.allergyNote ?: "알레르기 성분"} 포함"
        }

        val text = lines.joinToString("\n")
        val type = if (hasWarn) Banner.Type.WARNING else Banner.Type.INFO
        return Banner(type, text)
    }

    /** 음성: 기존 로직 유지 (원하면 동일하게 한 줄로 합쳐 읽도록 커스터마이즈 가능) */
    fun toVoice(r: DetectResult): Voice {
        val priceText = r.price?.let { "가격은 ${formatPrice(it)}원" } ?: ""
        val promoText = r.promo?.let { ", 행사 ${it}" } ?: ""
        val warnText  = if (r.hasAllergy) ", 주의 성분 포함" else ""
        return Voice("${r.name}를 찾았습니다. $priceText$promoText$warnText.")
    }

    /** 장바구니 케이스(있다면 계속 사용 가능) */
    fun toCartBanner(r: DetectResult, inCart: Boolean): Banner {
        val base = toBanner(r)
        val text = if (inCart) base.text + "\n장바구니에 담긴 상품입니다. 제거할까요?" else base.text
        return base.copy(text = text)
    }

    fun toCartVoice(r: DetectResult, inCart: Boolean): Voice {
        val v = toVoice(r).text
        return if (inCart) Voice("$v 장바구니에 담긴 상품입니다. 제거할까요?") else Voice(v)
    }
}
