package com.example.reservationapp.data.model

interface User {
    val id: Int
    val userType: UserType
}

enum class UserType {
    CLIENT,
    PROVIDER,
}