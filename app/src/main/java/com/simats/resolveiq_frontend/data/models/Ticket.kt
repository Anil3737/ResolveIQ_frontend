package com.simats.resolveiq_frontend.data.models

data class Ticket(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,  // OPEN, IN_PROGRESS, RESOLVED, CLOSED, ESCALATED
    val priority: String,  // P1, P2, P3, P4
    val department_id: Int,
    val created_by: Int,
    val assigned_to: Int?,
    val created_at: String,
    val updated_at: String?,
    val sla_hours: Int?,
    val sla_deadline: String?,
    val resolved_at: String?
) {
    // Helper methods for status checks
    fun isOpen() = status == "OPEN"
    fun isResolved() = status == "RESOLVED" || status == "CLOSED"
    fun isCritical() = priority == "P1"
    fun isHighPriority() = priority == "P1" || priority == "P2"
    
    // Helper to get priority color
    fun getPriorityColor(): String {
        return when (priority) {
            "P1" -> "#EF4444"  // Red
            "P2" -> "#F97316"  // Orange
            "P3" -> "#EAB308"  // Yellow
            "P4" -> "#22C55E"  // Green
            else -> "#6B7280"  // Gray
        }
    }
    
    // Helper to get status color
    fun getStatusColor(): String {
        return when (status) {
            "OPEN" -> "#3B82F6"       // Blue
            "IN_PROGRESS" -> "#8B5CF6"  // Purple
            "RESOLVED" -> "#22C55E"    // Green
            "CLOSED" -> "#6B7280"      // Gray
            "ESCALATED" -> "#DC2626"   // Darker Red
            else -> "#6B7280"
        }
    }
}
