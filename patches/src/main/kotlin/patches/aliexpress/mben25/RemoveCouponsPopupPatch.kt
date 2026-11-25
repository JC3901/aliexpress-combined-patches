/*
 * Ported from mben25/morphe-patches (https://github.com/mben25/morphe-patches), GPL-3.0.
 * Rewritten for AliExpress 8.180.1.
 */
package patches.aliexpress.mben25

import patches.aliexpress.ALIEXPRESS
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * In 8.162.8 the app decided locally (`CouponFloorFatigueManager.isFatigued`) whether the
 * search-results coupon floor/popup had been shown often enough today. In 8.180.1 that check
 * moved server-side: the app only reports how many times it has shown the floor today
 * (`dayFloorFatigue`) and the server decides. Reporting the daily limit as already reached keeps
 * the floor hidden, which is what the original patch achieved.
 */
private const val SHOWN_TODAY = "0x63" // 99

/** Image search: `CouponFloorFatigueManager.a()I`, today's shown count. */
private object ImageSearchCouponCountFingerprint : Fingerprint(
    definingClass = "Lcom/alibaba/aliexpress/android/search/core/pop/couponfloor/CouponFloorFatigueManager;",
    returnType = "I",
    parameters = emptyList(),
    custom = { method, _ -> method.accessFlags and AccessFlags.STATIC.value != 0 },
)

/** Text/category search and recommendations: `CouponFloorFatigueUseCase.a()I` (obfuscated class `c`). */
private object SearchCouponCountFingerprint : Fingerprint(
    returnType = "I",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type.startsWith("Lcom/aliexpress/android/aesearch/common/couponfatigue/") &&
            method.name != "hashCode" &&
            method.accessFlags and AccessFlags.STATIC.value == 0
    },
)

@Suppress("unused")
val removeCouponsPopupPatch = bytecodePatch(
    name = "Remove coupons popup",
    description = "Hides the coupon floor/popup shown over search results.",
    default = true,
) {
    compatibleWith(ALIEXPRESS)

    execute {
        val methods = listOfNotNull(
            ImageSearchCouponCountFingerprint.methodOrNull,
            SearchCouponCountFingerprint.methodOrNull,
        )
        if (methods.isEmpty()) throw PatchException("AliExpress coupon floor counters not found")
        methods.forEach { method ->
            method.addInstructions(
                0,
                """
                    const/16 v0, $SHOWN_TODAY
                    return v0
                """,
            )
        }
    }
}
