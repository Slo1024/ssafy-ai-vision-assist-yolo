package com.example.lookey.data.remote.dto.navigation

data class VisionAnalyzeResponse(
    val success: Boolean,
    val message: String? = null,
    val data: VisionData? = null,
    val error: String? = null,
    val timestamp: Long? = null
)

data class VisionData(
    val people: LRFSides = LRFSides(),
    val directions: LRFSides = LRFSides(),
    val category: String? = null,
    val obstacles: LRFSides = LRFSides(),
    val counter: Boolean = false
)

data class LRFSides(
    val left: Boolean = false,
    val front: Boolean = false,
    val right: Boolean = false
)
