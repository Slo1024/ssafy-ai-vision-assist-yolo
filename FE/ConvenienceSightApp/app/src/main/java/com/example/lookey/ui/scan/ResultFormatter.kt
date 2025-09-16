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

    // ⬇️ 한 줄: "이름 | 1,700원 | 2+1 행사품입니다."
    // 알레르기 있으면 2줄: (한 줄) + "\n주의: ..."
    fun toBanner(r: DetectResult): Banner {
        val parts = buildList {
            add(r.name)
            r.price?.let { add("${formatPrice(it)}원") }
            r.promo?.let { add("${it} 행사품입니다.") }
        }
        val line1 = parts.joinToString(" | ")

        val hasWarn = r.hasAllergy
        val text = if (hasWarn) {
            line1 + "\n주의: ${r.allergyNote ?: "알레르기 성분"} 포함"
        } else {
            line1
        }

        val type = if (hasWarn) Banner.Type.WARNING else Banner.Type.INFO
        return Banner(type, text)
    }

    fun toVoice(r: DetectResult): Voice {
        val priceText = r.price?.let { "가격은 ${formatPrice(it)}원" } ?: ""
        val promoText = r.promo?.let { ", 행사 ${it}" } ?: ""
        val warnText  = if (r.hasAllergy) ", 주의 성분 포함" else ""
        return Voice("${r.name}를 찾았습니다. $priceText$promoText$warnText.")
    }

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
