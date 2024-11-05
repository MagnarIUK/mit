package com.magnariuk.mittest.util.util

/**
 * Повертає значення в пікселях, наприклад: 25.px -> "25px".
 */
val Int.px: String
    get() = "${this}px"

/**
 * Повертає значення в відсотках, наприклад: 25.px -> "25%".
 */
val Int.p: String
    get() = "${this}%"

/**
 * Повертає значення в секундах, наприклад: 1.2.s -> "1.2s".
 */
val Double.s: String get() = "${this}s"

/**
 * Повертає значення в hex кольорі, наприклад: "d3d3d3".p -> "#d3d3d3".
 */
val String.p: String get() = "#${this}"