package com.rizek.tiebreaker.util

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

sealed class RegisterResult {
    object Success : RegisterResult()
    object UserAlreadyExists : RegisterResult()
    object InvalidInput : RegisterResult()
}

class AuthManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "tiebreaker_auth"
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
        private const val ITERATIONS = 65536
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 16
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun registerUser(username: String, password: String): RegisterResult {
        val normalized = username.trim().lowercase()
        if (normalized.length < 3 || password.length < 6) {
            return RegisterResult.InvalidInput
        }
        if (prefs.contains("user_${normalized}_hash")) {
            return RegisterResult.UserAlreadyExists
        }

        val salt = generateSalt()
        val hash = hashPassword(password, salt)

        prefs.edit()
            .putString("user_${normalized}_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("user_${normalized}_hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()

        return RegisterResult.Success
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val normalized = username.trim().lowercase()
        val storedSaltB64 = prefs.getString("user_${normalized}_salt", null) ?: return false
        val storedHashB64 = prefs.getString("user_${normalized}_hash", null) ?: return false

        val salt = Base64.decode(storedSaltB64, Base64.NO_WRAP)
        val storedHash = Base64.decode(storedHashB64, Base64.NO_WRAP)
        val computedHash = hashPassword(password, salt)

        return storedHash.contentEquals(computedHash)
    }

    fun isUserLoggedIn(): Boolean = prefs.getString(KEY_LOGGED_IN_USER, null) != null

    fun getCurrentUser(): String? = prefs.getString(KEY_LOGGED_IN_USER, null)

    fun setLoggedIn(username: String) {
        prefs.edit().putString(KEY_LOGGED_IN_USER, username.trim().lowercase()).apply()
    }

    fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
