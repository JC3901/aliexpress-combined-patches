# AliExpress combined Morphe patches (8.180.1)

One patch bundle for AliExpress 8.180.1 (versionCode 80005998), combining:

| Patch | Source |
|---|---|
| Disable analytics, Disable update prompts, Enable image saving, Fix dark mode, GmsCore support, Remove promotional popups, Remove sponsored items, Skip splash promotions, Spoof signature | hxreborn/morphe-patches, via johninnld/nl-morphe-patches |
| Remove promotions and games | johninnld/nl-morphe-patches |
| Block marketing pop-up layers (mben25's "Remove ads"), Disable promotions notifications, Remove affiliate tracking, Remove coupons popup | mben25/morphe-patches, retargeted from 8.162.8 |
| Skip bundle deals | new: in-app version of the "Bundle Buster" userscript idea, without its affiliate redirect |

mben25's patches that duplicate the others (dark mode, splash, analytics, updates, image saving,
sponsored items) are left out so two patches never fight over the same feature.

## Changes from upstream

* **Two different 8.180.1 builds exist.** johninnld's Nov 13 2026 update renamed several pinned
  classes for one 8.180.1 build (w9.a, wx.a, lt.d, ...), which broke "Remove sponsored items" and
  "Remove promotions and games" on the other build (e.g. the APKMirror arm64-v8a + armeabi-v7a
  APK), which keeps the earlier names (l9.a, su.a, cr.c, ...). `BuildVariant.kt` detects which
  build is being patched and those two patches pick the matching names, so both builds work.
* **Remove coupons popup** was rewritten: 8.180.1 dropped the local `isFatigued` check and now
  sends the server a "times shown today" count. The patch reports the daily limit as reached.
* **Skip bundle deals** hooks the app's link router (`com.aliexpress.service.nav.Nav`). Links to
  `/ssr/300000512/...` or `...BundleDeals...` pages that carry a product ID (`productIds`,
  `productId` or `pid`) open `/item/<id>.html` instead. Bundle pages without a product ID
  are left alone.

## Build

Needs JDK 21 and the Android SDK (`local.properties` -> `sdk.dir=...`).

    ./gradlew :patches:build                 # -> patches/build/libs/patches-<version>.mpp
    ./gradlew :patches:applyToApk -Papk=/path/to/aliexpress-8.180.1.apk   # dry-run every patch

Licence: GPL-3.0 (all three sources are GPL-3.0). Keep the upstream notices.
