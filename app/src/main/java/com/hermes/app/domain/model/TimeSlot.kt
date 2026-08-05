package com.hermes.app.domain.model

data class TimeSlot(
    val startTimestamp: Long,
    val endTimestamp: Long,
) {
    val durationMinutes: Long
        get() = (endTimestamp - startTimestamp) / (1000 * 60)

    fun contains(timestamp: Long): Boolean {
        return timestamp in startTimestamp..endTimestamp
    }

    fun overlapsWith(other: TimeSlot): Boolean {
        return startTimestamp < other.endTimestamp && endTimestamp > other.startTimestamp
    }
}
