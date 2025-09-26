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
     * ✅ TTS 한국어 단위 교정
     * - "500ml", "500 ML", "500mL" 등 → "500 밀리리터"
     * - 필요 시 여기에 g/kg/ℓ 등도 추가 확장 가능
     */
    fun normalizeTtsKo(text: String): String {
        var t = text

        // ml (대소문자/특수문자 ℓ/㎖ 포함, 붙여 쓰기/띄어쓰기 모두 커버)
        // 예: 200ML, 200ml, 200mL, 200 mℓ, 200㎖
        t = t.replace(Regex("(?i)\\b(\\d+)\\s*m\\s*[lℓ]\\b"), "$1 밀리리터")
        t = t.replace(Regex("(?i)\\b(\\d+)\\s*ml\\b"), "$1 밀리리터")
        t = t.replace(Regex("\\b(\\d+)\\s*[㎖]\\b"), "$1 밀리리터")
        // 혹시 'ML.' 처럼 마침표가 붙은 케이스
        t = t.replace(Regex("(?i)\\b(\\d+)\\s*ml\\."), "$1 밀리리터")

        // 필요시 추가 단위도 확장 가능:
        // t = t.replace(Regex("(?i)\\b(\\d+)\\s*g\\b"), "$1 그램")
        // t = t.replace(Regex("(?i)\\b(\\d+)\\s*l\\b"), "$1 리터")

        return t
    }



    //** 2+1 → "이 플러스 일", "행사품입니다." → "행사 상품입니다." 등 TTS 친화 변환 */
    fun normalizeBannerKo(text: String): String {
        var t = text

        // 파이프 → 쉼표로 (TTS가 전부 읽도록)
        t = t.replace("|", ", ")

        // 2+1, 1+1 등 → 한국어 발음
        t = t.replace(Regex("\\b1\\+1\\b"), "원 플러스 원")
        t = t.replace(Regex("\\b2\\+1\\b"), "이 플러스 일")
        t = t.replace(Regex("\\b(\\d)\\+(\\d)\\b")) { m ->
            val d = arrayOf("영","일","이","삼","사","오","육","칠","팔","구")
            "${d[m.groupValues[1].toInt()]} 플러스 ${d[m.groupValues[2].toInt()]}"
        }

        // 행사품입니다 → 행사 상품입니다 (연음 끊기)
        t = t.replace("행사품입니다", "행사 상품입니다")

        // 숫자+ml → 기존 한국어 단위 교정 재사용
        t = normalizeTtsKo(t)

        return t
    }






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
        val priceText = r.price?.let  { "가격은 ${formatPrice(it)}원" } ?: ""
        val promoText = r.promo?.let { ", 행사 ${it}" } ?: ""
        val warnText  = if (r.hasAllergy) ", 주의 성분 포함" else ""
        val raw = "${r.name}를 찾았습니다. $priceText$promoText$warnText."
        return Voice(normalizeTtsKo(raw))  // ✅ TTS 교정 적용
    }

    fun toCartBanner(r: DetectResult, inCart: Boolean): Banner {
        val base = toBanner(r)
        val text = if (inCart) base.text + "\n장바구니에 담긴 상품입니다. 제거할까요?" else base.text
        return base.copy(text = text)
    }

    fun toCartVoice(r: DetectResult, inCart: Boolean): Voice {
        val v = toVoice(r).text
        val raw = if (inCart) "$v 장바구니에 담긴 상품입니다. 제거할까요?" else v
        return Voice(normalizeTtsKo(raw)) // ✅ 보이스 합성에도 교정 적용
    }
}
