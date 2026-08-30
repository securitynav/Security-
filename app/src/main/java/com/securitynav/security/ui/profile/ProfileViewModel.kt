package com.securitynav.security.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.securitynav.security.data.UserRepository
import com.securitynav.security.data.User

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadProfile() {
        val user = userRepository.getUser()
        if (user != null) {
            _userName.value = user.name
        } else {
            _errorMessage.value = "Usuario no cargado"
        }
    }
}
