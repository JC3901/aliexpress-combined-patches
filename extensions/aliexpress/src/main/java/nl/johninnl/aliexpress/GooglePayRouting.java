/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * GmsCore support sends every Google Play services call to MicroG-RE. MicroG's Google Pay (Wallet)
 * service is a stub that always answers "not ready to pay", so the Google Pay button never works.
 * This sends only the Google Pay service binding to the real Google Play services, if installed.
 */
package nl.johninnl.aliexpress;

import android.content.Intent;

@SuppressWarnings("unused")
public final class GooglePayRouting {

    // Built at run time: GmsCore support rewrites every "com.google.android.gms" string constant in
    // the app, including in this class, to MicroG-RE's package.
    private static final String GOOGLE_PLAY_SERVICES = String.join(".", "com", "google", "android", "gms");

    private static final String WALLET_BIND_SUFFIX = ".wallet.service.BIND";

    private GooglePayRouting() {
    }

    /**
     * Called before the app builds the intent to bind to a Google Play services API.
     *
     * @param action the service action, possibly already renamed for MicroG-RE.
     * @return an intent for Google Pay on the real Google Play services, or null for any other API.
     */
    public static Intent googlePayIntent(String action) {
        if (action == null || !action.endsWith(WALLET_BIND_SUFFIX)) return null;
        return new Intent(GOOGLE_PLAY_SERVICES + WALLET_BIND_SUFFIX).setPackage(GOOGLE_PLAY_SERVICES);
    }
}
