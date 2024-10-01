package com.example.reservationapp.data.model

class Provider(
    override val id: Int,
    val description: String,
    override val userType: UserType = UserType.PROVIDER,
) : User