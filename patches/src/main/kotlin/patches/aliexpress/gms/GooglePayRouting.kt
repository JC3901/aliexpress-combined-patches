/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Keeps Google Pay working with GmsCore support: MicroG's Wallet service is a stub that always
 * reports "not ready to pay", so only the Google Pay binding goes to the real Google Play services.
 */
package patches.aliexpress.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.util.getReference
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import patches.aliexpress.skipPatch

private const val GOOGLE_PAY_ROUTING = "Lnl/johninnl/aliexpress/GooglePayRouting;"

/**
 * The Play services client's service endpoint (`GmsServiceEndpoint`, obfuscated) method that turns
 * its service action into the Intent used to bind: `new Intent(action).setPackage(gmsPackage)`.
 */
private object ServiceEndpointIntentFingerprint : Fingerprint(
    returnType = "Landroid/content/Intent;",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("serviceActionBundleKey", "serviceIntentCall"),
)

internal fun BytecodePatchContext.routeGooglePayToGooglePlayServices() {
    val method = ServiceEndpointIntentFingerprint.methodOrNull
        ?: return skipPatch("GmsCore support (Google Pay)", "Play services endpoint not found; Google Pay stays on MicroG-RE")

    // The method starts by reading the endpoint's service action field.
    val first = method.getInstruction(0)
    val actionField = first.takeIf { it.opcode == Opcode.IGET_OBJECT }?.getReference<FieldReference>()
        ?.takeIf { it.type == "Ljava/lang/String;" }
        ?: return skipPatch("GmsCore support (Google Pay)", "service action field not found")

    method.addInstructionsWithLabels(
        0,
        """
            iget-object v0, p0, $actionField
            invoke-static { v0 }, $GOOGLE_PAY_ROUTING->googlePayIntent(Ljava/lang/String;)Landroid/content/Intent;
            move-result-object v0
            if-eqz v0, :original
            return-object v0
        """,
        ExternalLabel("original", first),
    )
}
