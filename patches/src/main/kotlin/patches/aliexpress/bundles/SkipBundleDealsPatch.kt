/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * In-app equivalent of the "Bundle Buster" userscript idea: bundle-deal landing pages for a
 * product open that product's page instead. Original implementation, without affiliate routing.
 */
package patches.aliexpress.bundles

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import patches.aliexpress.skipPatch
import com.android.tools.smali.dexlib2.AccessFlags
import patches.aliexpress.ALIEXPRESS

private const val NAV = "Lcom/aliexpress/service/nav/Nav;"
private const val EXTENSION = "Lnl/johninnl/aliexpress/BundleDealSkipper;"

private fun isInstance(flags: Int) = flags and AccessFlags.STATIC.value == 0

/** `Nav.toUri(String)` (obfuscated `g` in 8.180.1): every in-app link opened by URL string. */
private object NavOpenUrlFingerprint : Fingerprint(
    definingClass = NAV,
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    custom = { method, _ -> isInstance(method.accessFlags) },
)

/** `Nav.toUri(Uri)` (obfuscated `f` in 8.180.1): links opened by parsed Uri. */
private object NavOpenUriFingerprint : Fingerprint(
    definingClass = NAV,
    returnType = "Z",
    parameters = listOf("Landroid/net/Uri;"),
    custom = { method, _ -> isInstance(method.accessFlags) },
)

@Suppress("unused")
val skipBundleDealsPatch = bytecodePatch(
    name = "Skip bundle deals",
    description = "Opens the product page directly instead of the \"Bundle deals\" / Choice landing page " +
        "when you tap a bundle-deal product.",
) {
    compatibleWith(ALIEXPRESS)
    extendWith("extensions/aliexpress.mpe")

    execute {
        val urlMethod = NavOpenUrlFingerprint.methodOrNull
            ?: return@execute skipPatch("Skip bundle deals", "link router not found")
        urlMethod.addInstructions(
            0,
            """
                invoke-static/range { p1 .. p1 }, $EXTENSION->rewrite(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
            """,
        )
        NavOpenUriFingerprint.methodOrNull?.addInstructions(
            0,
            """
                invoke-static/range { p1 .. p1 }, $EXTENSION->rewrite(Landroid/net/Uri;)Landroid/net/Uri;
                move-result-object p1
            """,
        )
    }
}
