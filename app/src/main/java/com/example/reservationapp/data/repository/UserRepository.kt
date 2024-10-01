package com.example.reservationapp.data.repository

import com.example.reservationapp.data.model.User
import com.example.reservationapp.data.model.UserType

interface UserRepository {
    suspend fun login(username: String, password: String, userType: UserType): User
    fun getCurrentUser(): User?
}