package com.elektro.monitoring.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elektro.monitoring.data.network.AuthState
import com.elektro.monitoring.data.network.CheckState
import com.elektro.monitoring.data.network.UserState
import com.elektro.monitoring.data.repo.AuthRepository
import com.elektro.monitoring.helper.Valid
import com.elektro.monitoring.helper.utils.Resource
import com.elektro.monitoring.model.ErrorModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val _user = MutableStateFlow(AuthState())
    val user: StateFlow<AuthState> = _user

    private val _userData = MutableStateFlow(UserState())
    val userData: StateFlow<UserState> = _userData

    private val _check = MutableStateFlow(CheckState())
    val check: StateFlow<CheckState> = _check

    val file: MutableLiveData<Uri> = MutableLiveData()
    var name: MutableLiveData<String> = MutableLiveData()
    var nomor: MutableLiveData<String> = MutableLiveData()
    var email: MutableLiveData<String> =  MutableLiveData()
    var password: MutableLiveData<String> = MutableLiveData()
    var newPassword: MutableLiveData<String> = MutableLiveData()
    var confirmPassword: MutableLiveData<String> = MutableLiveData()

    val errorModel: MutableLiveData<ErrorModel> = MutableLiveData()
    var shouldNavigateUp: MutableLiveData<Boolean> = MutableLiveData()

    init {
        errorModel.value = ErrorModel()
        shouldNavigateUp.value = false
    }

    fun onClickLogin(){
        if (checkFieldLogin()) {
            if (validateLogIn()) {
                login()
            }
        }
    }

    fun onClickUpdate(){
        if (checkFieldUpdate()) {
            if (checkPassword()) {
                if (validateUpdate()) {
                    updatePass()
                }
            }
        }
    }

    fun onClickResgister(){
        if (checkFieldRegister()){
            if (checkPassword()){
                if (validateRegister()){
                    register()
                }
            }
        }
    }

    private fun checkPassword(): Boolean {
        val error = ErrorModel()

        return if (Valid.isConfirmPassword(newPassword.value.toString(), confirmPassword.value.toString())){
            true
        } else {
            error.confirmPasswordErrorMessage = "Passwords do not match."
            error.newPasswordErrorMessage = "Passwords do not match."
            errorModel.value = error
            false
        }
    }

    private fun validateLogIn() : Boolean{
        val error = ErrorModel()
        val isValidEmail = Valid.isValidEmail(email.value.toString().trim())
        val isValidPassword = Valid.isValidPassword(password.value.toString().trim())

        if(!isValidEmail && !isValidPassword){
            error.passwordErrorMessage = "Please Enter Valid Password"
            error.emailErrorMessage = "Please Enter Valid Email"
        } else if(!isValidPassword){
            error.passwordErrorMessage = "Please Enter Valid Password"
        } else if(!isValidEmail){
            error.emailErrorMessage = "Please Enter Valid Email"
        }
        errorModel.value = error

        return isValidEmail && isValidPassword
    }

    private fun validateUpdate(): Boolean {
        val error = ErrorModel()
        val isCurrentPassword = Valid.isValidPassword(password.value.toString().trim())
        val isNewPassword = Valid.isValidPassword(newPassword.value.toString().trim())

        if (this.password.value == newPassword.value) {
            error.newPasswordErrorMessage = "New password cannot be the same as the old password."
        } else {
            if (!isCurrentPassword && !isNewPassword) {
                error.passwordErrorMessage = "Please Enter Valid Password."
                error.newPasswordErrorMessage = "Please Enter Valid Password."
                error.confirmPasswordErrorMessage = "Please Enter Valid Password."
            } else if (!isNewPassword) {
                error.newPasswordErrorMessage = "Please Enter Valid Password."
                error.confirmPasswordErrorMessage = "Please Enter Valid Password."
            } else if (!isCurrentPassword) {
                error.passwordErrorMessage = "Please Enter Valid Password."
            }
        }
        errorModel.value = error

        return isCurrentPassword && isNewPassword
    }

    private fun validateRegister(): Boolean {
        val error = ErrorModel()
        val isValidEmail = Valid.isValidEmail(email.value.toString().trim())
        val isValidPassword = Valid.isValidPassword(newPassword.value.toString().trim())

        if(!isValidEmail && !isValidPassword){
            error.passwordErrorMessage = "Please Enter Valid Password"
            error.emailErrorMessage = "Please Enter Valid Email"
        } else if(!isValidPassword){
            error.passwordErrorMessage = "Please Enter Valid Password"
        } else if(!isValidEmail){
            error.emailErrorMessage = "Please Enter Valid Email"
        }
        errorModel.value = error

        return isValidEmail && isValidPassword
    }

    private fun checkFieldLogin(): Boolean {
        val error = ErrorModel()

        return if (email.value.isNullOrBlank() && password.value.isNullOrBlank()){
            error.emailErrorMessage = "Must not be empty"
            error.passwordErrorMessage = "Must not be empty"
            errorModel.value = error
            false
        } else {
            true
        }
    }

    private fun checkFieldUpdate(): Boolean {
        val error = ErrorModel()

        return if (password.value.isNullOrBlank() && newPassword.value.isNullOrBlank()
            && confirmPassword.value.isNullOrBlank()){
            error.newPasswordErrorMessage = "Must not be empty"
            error.confirmPasswordErrorMessage = "Must not be empty"
            error.passwordErrorMessage = "Must not be empty"
            errorModel.value = error
            false
        } else {
            true
        }
    }

    private fun checkFieldRegister(): Boolean {
        val error = ErrorModel()

        return if (name.value.isNullOrBlank() && newPassword.value.isNullOrBlank()
            && confirmPassword.value.isNullOrBlank() && email.value.isNullOrBlank()
            && nomor.value.isNullOrBlank()){
            error.nameErrorMessage = "Must not be empty"
            error.newPasswordErrorMessage = "Must not be empty"
            error.confirmPasswordErrorMessage = "Must not be empty"
            error.nomorErrorMessage = "Must not be empty"
            error.emailErrorMessage = "Must not be empty"
            errorModel.value = error
            false
        } else {
            true
        }
    }

    private fun login() {
        authRepository.login(email.value.toString(), this.password.value.toString()).onEach {
            when (it) {
                is Resource.Loading -> {
                    _user.value = AuthState(isLoading = true)
                }
                is Resource.Error -> {
                    _user.value = AuthState(error = it.message ?: "")
                }
                is Resource.Success -> {
                    _user.value = AuthState(data = it.data)
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun updatePass() {
        authRepository.changePassword(userData.value.data!!.email,
            password.value.toString(), newPassword.value.toString()).onEach {
            when (it) {
                is Resource.Loading -> {
                    _check.value = CheckState(isLoading = true)
                }
                is Resource.Error -> {
                    _check.value = CheckState(error = it.message ?: "")
                }
                is Resource.Success -> {
                    _check.value = CheckState(data = it.data)
                    shouldNavigateUp.value = it.data!!
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun register() {
        authRepository.register(email.value.toString(), newPassword.value.toString(),
            file.value!!, name.value.toString(), nomor.value.toString()).onEach {
            when (it) {
                is Resource.Loading -> {
                    _check.value = CheckState(isLoading = true)
                }
                is Resource.Error -> {
                    _check.value = CheckState(error = it.message ?: "")
                }
                is Resource.Success -> {
                    _check.value = CheckState(data = it.data)
                    shouldNavigateUp.value = it.data!!
                }
            }
        }.launchIn(viewModelScope)
    }

    fun loggedUser() {
        authRepository.getLoggedUser().onEach {
            when (it) {
                is Resource.Loading -> {
                    _user.value = AuthState(isLoading = true)
                }
                is Resource.Error -> {
                    _user.value = AuthState(error = it.message ?: "")
                }
                is Resource.Success -> {
                    _user.value = AuthState(data = it.data)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getUserData() {
        authRepository.getUserData().onEach {
            when (it) {
                is Resource.Loading -> {
                    _userData.value = UserState(isLoading = true)
                }
                is Resource.Error -> {
                    _userData.value = UserState(error = it.message ?: "")
                }
                is Resource.Success -> {
                    _userData.value = UserState(data = it.data)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun logOut() {
        viewModelScope.launch {
            authRepository.logOut()
        }
    }
}