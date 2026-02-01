package com.rafael.inclusimap.core.util

import kotlin.system.exitProcess

actual fun exitProcess(code: Int): Nothing = exitProcess(code)
