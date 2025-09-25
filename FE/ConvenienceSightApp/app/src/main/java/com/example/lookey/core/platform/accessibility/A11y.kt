package com.example.lookey.core.platform.accessibility

import android.content.Context
import android.view.accessibility.AccessibilityManager

object A11y {
    fun isScreenReaderOn(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return am.isEnabled && am.isTouchExplorationEnabled
    }
}