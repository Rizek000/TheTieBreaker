package com.rizek.tiebreaker.model

data class DecisionResult(
    val winner: String,
    val options: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)
