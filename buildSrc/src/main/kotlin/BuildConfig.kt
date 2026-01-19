import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object BuildConfig {

    const val ApplicationId = "io.github.pndhd1.sleeptimer"

    const val CompileSdk = 36
    const val TargetSdk = 36
    const val MinSdk = 24
    val Java = JavaVersion.VERSION_11
    val Jvm = JvmTarget.JVM_11

    const val VersionCode = 1

    const val MajorVersion = 0
    const val MinorVersion = 0
    const val PatchVersion = 1
    const val VersionName = "$MajorVersion.$MinorVersion.$PatchVersion"
}