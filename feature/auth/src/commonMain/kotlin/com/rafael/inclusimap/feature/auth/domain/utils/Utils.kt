package com.rafael.inclusimap.feature.auth.domain.utils

fun String.isValidEmail(): Boolean {
    val emailRegex = Regex(
        "(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*" +
            "|\"(?:[\u0001-\b\u000B\u000c\u000E-\u001F!#-]-~|\\\\[\u0001-\t\u000B\u000c\u000E-\u007F])*\")@" +
            "(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+" +
            "[a-zA-Z]{2,}|" +
            "\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|" +
            "[a-zA-Z0-9-]*[a-zA-Z0-9]:(" +
            "?:[\u0001-\b\u000B\u000c\u000E-\u001F!-Z^-~]|" +
            "\\\\[\u0001-\t\u000B\u000c\u000E-\u007F])+)])",
    )
    return emailRegex.matches(this)
}

fun isValidPassword(password: String): Boolean {
    val passwordRegex = Regex("""^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_#])[A-Za-z\d@$!%*?&_#]{8,}$""")
    return passwordRegex.matches(password)
}
