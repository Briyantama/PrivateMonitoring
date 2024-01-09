package com.elektro.monitoring.data.network

import com.elektro.monitoring.model.User

data class UserState(
    val data: User? = null,
    val error: String = "",
    val isLoading: Boolean = false
)