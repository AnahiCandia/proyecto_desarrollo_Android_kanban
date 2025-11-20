package com.example.greetingcard

import androidx.annotation.StringRes

data class Task(
    val id: Int,
    val title: String,
    val category: TaskCategory,
    var status: TaskStatus
)

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}

enum class TaskCategory(@StringRes val displayName: Int) {
    SUPERMERCADO(R.string.category_supermarket),
    FARMACIA(R.string.category_pharmacy),
    HOGAR(R.string.category_home)
}
