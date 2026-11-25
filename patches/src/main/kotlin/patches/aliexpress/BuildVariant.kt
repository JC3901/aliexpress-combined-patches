package patches.aliexpress

import app.morphe.patcher.patch.BytecodePatchContext

/**
 * Two different AliExpress builds are both published as 8.180.1 (versionCode 80005998), with
 * different obfuscated class names. Build "A" (e.g. the APKMirror arm64-v8a + armeabi-v7a
 * universal APK) keeps the 8.180.0-era names (l9.a, su.a, cr.c, ...); build "B" renamed them
 * (w9.a, wx.a, lt.d, ...). Hooks call [detectAliExpressBuild] first and then pick names with [ae].
 */
internal object AliExpressBuild {
    @Volatile var renamed: Boolean = true
}

internal fun BytecodePatchContext.detectAliExpressBuild() {
    AliExpressBuild.renamed = classDefByOrNull("Lwx/a;") != null
}

/** Returns [renamed] on the renamed build and [original] on the 8.180.0-style build. */
internal fun <T> ae(renamed: T, original: T): T = if (AliExpressBuild.renamed) renamed else original
