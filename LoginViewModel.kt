package com.example.example.ui.login

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.example.R
import com.example.example.data.LoginRepository

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(
        context: Context,
        username: String,
        password: String
    ) {

        loginRepository.login(context, username, password, callback = { (result) ->
            if (result != null) {
                _loginResult.postValue(LoginResult(success = LoggedInUserView(displayName = result.displayName)))

            } else {
                _loginResult.postValue(LoginResult(error = R.string.login_failed))

            }
        })
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password1)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }


    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }
}
