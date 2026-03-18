package com.rizek.tiebreaker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rizek.tiebreaker.R
import com.rizek.tiebreaker.databinding.ActivityLoginBinding
import com.rizek.tiebreaker.util.AuthManager
import com.rizek.tiebreaker.util.RegisterResult

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authManager: AuthManager
    private var isSignUpMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authManager = AuthManager(this)

        if (authManager.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAction.setOnClickListener { onActionClicked() }
        binding.toggleText.setOnClickListener { toggleMode() }
    }

    private fun toggleMode() {
        isSignUpMode = !isSignUpMode
        clearErrors()

        if (isSignUpMode) {
            binding.loginModeTitle.text = getString(R.string.signup_title)
            binding.btnAction.text = getString(R.string.btn_signup)
            binding.toggleText.text = getString(R.string.toggle_to_login)
            binding.inputConfirmPassword.visibility = View.VISIBLE
        } else {
            binding.loginModeTitle.text = getString(R.string.login_title)
            binding.btnAction.text = getString(R.string.btn_login)
            binding.toggleText.text = getString(R.string.toggle_to_signup)
            binding.inputConfirmPassword.visibility = View.GONE
        }
    }

    private fun onActionClicked() {
        clearErrors()

        val username = binding.inputUsername.editText?.text?.toString()?.trim() ?: ""
        val password = binding.inputPassword.editText?.text?.toString() ?: ""
        val confirmPassword = binding.inputConfirmPassword.editText?.text?.toString() ?: ""

        // Validate username
        if (username.isEmpty()) {
            binding.inputUsername.error = getString(R.string.error_empty_username)
            return
        }
        if (username.length < 3) {
            binding.inputUsername.error = getString(R.string.error_short_username)
            return
        }

        // Validate password
        if (password.isEmpty()) {
            binding.inputPassword.error = getString(R.string.error_empty_password)
            return
        }
        if (password.length < 6) {
            binding.inputPassword.error = getString(R.string.error_short_password)
            return
        }

        // Sign-up: validate confirm password
        if (isSignUpMode && password != confirmPassword) {
            binding.inputConfirmPassword.error = getString(R.string.error_password_mismatch)
            return
        }

        if (isSignUpMode) {
            handleSignUp(username, password)
        } else {
            handleLogin(username, password)
        }
    }

    private fun handleSignUp(username: String, password: String) {
        when (authManager.registerUser(username, password)) {
            is RegisterResult.Success -> {
                authManager.setLoggedIn(username)
                navigateToMain()
            }
            is RegisterResult.UserAlreadyExists -> {
                showError(getString(R.string.error_user_exists))
            }
            is RegisterResult.InvalidInput -> {
                showError(getString(R.string.error_short_username))
            }
        }
    }

    private fun handleLogin(username: String, password: String) {
        if (authManager.authenticateUser(username, password)) {
            authManager.setLoggedIn(username)
            navigateToMain()
        } else {
            showError(getString(R.string.error_invalid_credentials))
        }
    }

    private fun showError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun clearErrors() {
        binding.errorText.visibility = View.GONE
        binding.inputUsername.error = null
        binding.inputPassword.error = null
        binding.inputConfirmPassword.error = null
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
