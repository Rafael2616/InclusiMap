package com.rafael.inclusimap.feature.auth.domain.utils

import android.util.Patterns

fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun isValidPassword(password: String): Boolean {
    val passwordRegex = Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_#])[A-Za-z\d@$!%*?&_#]{8,}$""")
    return passwordRegex.matches(password)
}
