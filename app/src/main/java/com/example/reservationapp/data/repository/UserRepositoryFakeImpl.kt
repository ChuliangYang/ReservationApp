package com.example.reservationapp.data.repository

import com.example.reservationapp.data.model.Client
import com.example.reservationapp.data.model.Provider
import com.example.reservationapp.data.model.User
import com.example.reservationapp.data.model.UserType
import com.example.reservationapp.data.repository.UserRepository
import javax.inject.Inject

class UserRepositoryFakeImpl @Inject constructor() : UserRepository {
    private val fakeClient = Client(1)
    private val fakeProvider = Provider(2, "This is fake provider description")

    private var currentUser: User? = null

    override suspend fun login(username: String, password: String, userType: UserType): User {
        currentUser = when (userType) {
            UserType.CLIENT -> return fakeClient
            UserType.PROVIDER -> return fakeProvider
        }
        return currentUser!!
    }

    override fun getCurrentUser(): User? {
        return fakeClient
    }
}