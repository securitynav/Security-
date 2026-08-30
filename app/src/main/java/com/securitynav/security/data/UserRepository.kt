package com.securitynav.security.data

class UserRepository {
    private var cachedUser: User? = User("SecurityNav Admin")

    fun getUser(): User? {
        return cachedUser
    }

    fun setUser(user: User?) {
        cachedUser = user
    }
}

data class User(val name: String)
