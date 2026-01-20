package io.github.pndhd1.sleeptimer.utils.exceptions

class AdLoadException(
    code: Int,
    description: String,
) : Exception("Ad failed to load: $description (code: $code)")
