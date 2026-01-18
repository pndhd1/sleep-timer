package io.github.pndhd1.sleeptimer.utils.exceptions

/**
 * Exception for critical errors that should be prioritized in Crashlytics.
 * Used when the app recovers but the error is severe enough to warrant immediate attention.
 */
class FatalException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
