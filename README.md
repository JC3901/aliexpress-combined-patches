# AliExpress patches for Morphe

[![Add to Morphe](https://img.shields.io/badge/Add%20to-Morphe-1E5AA8?style=for-the-badge)](https://morphe.software/add-source?github=JC3901/aliexpress-combined-patches)

Patches for the [AliExpress](https://play.google.com/store/apps/details?id=com.alibaba.aliexpresshd) Android app
(`com.alibaba.aliexpresshd`), applied with [Morphe](https://morphe.software). No root needed.

They:

- **remove ads and sponsored items** from search results and product feeds;
- **block pop-ups**: promotional pop-ups, marketing pop-up layers (campaign, coupon and gift overlays) and the
  coupon pop-up over search results;
- **remove promotions and games**: coins, games and campaign sections;
- **skip bundle deals**: tapping a "Bundle deals" product opens the product page itself;
- **skip splash promotions** at start-up;
- **mute promotional notifications** (deals, campaigns, wishlist price drops, trends); order and message
  notifications still come through;
- **stop tracking**: analytics and affiliate tracking;
- **block update prompts**;
- **let you save product images**;
- **fix dark mode**;
- **sign in with Google through MicroG-RE**, while **Google Pay** keeps using your phone's own Google Play
  services (MicroG-RE can't take payments);
- **a Patches screen inside the app** with a switch for most of these, so you can turn one off without patching
  again.

Works with AliExpress **8.180.1** (versionCode 80005998).

The patches come from [hxreborn/morphe-patches](https://github.com/hxreborn/morphe-patches),
[johninnld/nl-morphe-patches](https://gitlab.com/johninnld/nl-morphe-patches) and
[mben25/morphe-patches](https://github.com/mben25/morphe-patches), put together into one bundle and fixed to work
on 8.180.1. Please [open an issue](https://github.com/JC3901/aliexpress-combined-patches/issues) if something
breaks.

## Install

1. Install Morphe Manager from [morphe.software](https://morphe.software), and
   [**MicroG-RE**](https://github.com/MorpheApp/MicroG-RE/releases) (the same one Morphe-patched YouTube uses).
   Open MicroG-RE and add your Google account to it if you want to sign in to AliExpress with Google.
2. Add this repository as a patch source: tap the **Add to Morphe** button above on your phone, or in Morphe
   Manager add `https://github.com/JC3901/aliexpress-combined-patches` as a source. If you added the
   hxreborn, johninnld or mben25 sources before, turn their AliExpress patches off, so no patch is applied twice.
3. Download **AliExpress 8.180.1** from
   [APKMirror](https://www.apkmirror.com/apk/alibaba-mobile/aliexpress-shopping-app/). Pick
   the file in Morphe Manager.
4. Morphe Manager patches with all the patches on. To leave some out, first turn on Settings > **Expert mode** in
   Morphe Manager; you then choose the patches before patching.
5. Uninstall the Play Store version of AliExpress first. The patched app is signed with a different key, so
   Android won't install it over the original.
6. Tap Install. If Google Play Protect says it hasn't seen the app before, tap **More details** and
   **Install without scanning**.

## Patch settings

Open **Account > Settings** in the patched app and tap **Patches**. It lists the patches you picked that can be
switched on and off in the app.

## How it differs from the original patches

- **Two different 8.180.1 builds exist.** johninnld's update of 13 November 2026 renamed several classes for
  one 8.180.1 build, which broke **Remove sponsored items** and **Remove promotions and games** on the other
  (such as the APKMirror arm64-v8a + armeabi-v7a APK). `BuildVariant.kt` detects which build is being patched,
  so both work.
- **Remove coupons popup** was rewritten. 8.180.1 no longer decides locally whether to show the coupon pop-up;
  it tells the server how many times it has shown it today. The patch reports the daily limit as reached.
- **Skip bundle deals** is new. It hooks the app's link router (`com.aliexpress.service.nav.Nav`): links to
  `/ssr/300000512/...` or `...BundleDeals...` pages that carry a product ID open `/item/<id>.html` instead.
  It is an in-app version of the idea behind the "Bundle Buster" userscript, written from scratch and without its
  affiliate redirect.
- mben25's patches that do the same job as hxreborn's (dark mode, splash, analytics, updates, image saving,
  sponsored items) are left out.

## Releasing a new version

Morphe Manager reads `patches-bundle.json` on `main` and downloads the bundle from the `releases/` folder, so
no GitHub release is needed.

1. Bump `version` in `gradle.properties` and run `./gradlew :patches:buildAndroid :patches:generatePatchesList`.
   Use `buildAndroid`, not `build`: only it adds the `classes.dex` that Morphe Manager needs on a phone.
   A bundle without it loads 0 patches.
2. Copy `patches/build/libs/patches-<version>.mpp` to `releases/patches-<version>.mpp`.
3. Update `version`, `download_url`, `created_at` and `description` in `patches-bundle.json` and push to
   `main`. Everyone who added the source gets the update in Morphe Manager. The bundle is unsigned, so the file
   must not declare a `public_key`.

## Build

Needs JDK 21 and the Android SDK (`local.properties` -> `sdk.dir=...`).

    ./gradlew :patches:buildAndroid          # -> patches/build/libs/patches-<version>.mpp
    ./gradlew :patches:applyToApk -Papk=/path/to/aliexpress-8.180.1.apk   # try every patch on an APK

## Licence

GPL-3.0, like all three sources. Keep the upstream notices in `NOTICE` and the file headers.
