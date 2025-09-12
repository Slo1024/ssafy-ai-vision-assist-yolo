package com.example.lookey.ui.scan

import com.example.lookey.domain.entity.DetectResult

object ResultFormatter {
    data class Banner(val type: Type, val text: String) {
        enum class Type { WARNING, INFO, SUCCESS }
    }
    data class Voice(val text: String)

    fun toBanner(r: DetectResult): Banner? = when {
        r.hasAllergy -> Banner(Banner.Type.WARNING, "주의: ${r.allergyNote ?: "알레르기 성분"} 포함")
        r.promo != null -> Banner(Banner.Type.INFO, "행사: ${r.promo}")
        else -> null
    }

    fun toVoice(r: DetectResult): Voice {
        val priceText = r.price?.let { "가격은 ${it}원" } ?: ""
        val promoText = r.promo?.let { ", 행사 ${it}" } ?: ""
        val warnText  = if (r.hasAllergy) ", 주의 성분 포함" else ""
        return Voice("${r.name}를 찾았습니다. $priceText$promoText$warnText.")
    }
}