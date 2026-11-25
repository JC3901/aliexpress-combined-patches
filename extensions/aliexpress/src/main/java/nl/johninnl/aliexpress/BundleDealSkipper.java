/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Skip bundle deals: when the app is asked to open an AliExpress "Bundle deals" / Choice landing
 * page for a specific product, open that product's own page instead. Original implementation;
 * no affiliate routing and nothing is sent anywhere.
 */
package nl.johninnl.aliexpress;

import android.net.Uri;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class BundleDealSkipper {

    /** Query parameters that carry the product the user tapped, in order of preference. */
    private static final String[] PRODUCT_PARAMS = {"productIds", "productId", "pid"};

    /** First run of 10+ digits; "productIds" values look like "<productId>:<skuId>,...". */
    private static final Pattern DIGITS = Pattern.compile("\\d{10,}");

    /** Shapes of real AliExpress product IDs. */
    private static final Pattern PRODUCT_ID =
            Pattern.compile("1005\\d{12}|3256\\d{12}|400[01]\\d{9}|3[23]\\d{9}");

    private BundleDealSkipper() {
    }

    /** Hook for the router's String entry point. Returns the input unchanged unless it is a bundle page. */
    public static String rewrite(String url) {
        if (url == null) return null;
        try {
            String target = productUrlFor(Uri.parse(url.trim()));
            return target != null ? target : url;
        } catch (Throwable ignored) {
            return url;
        }
    }

    /** Hook for the router's Uri entry point. */
    public static Uri rewrite(Uri uri) {
        if (uri == null) return null;
        try {
            String target = productUrlFor(uri);
            return target != null ? Uri.parse(target) : uri;
        } catch (Throwable ignored) {
            return uri;
        }
    }

    private static String productUrlFor(Uri uri) {
        String host = uri.getHost();
        String path = uri.getPath();
        if (host == null || path == null || !isAliExpressHost(host) || !isBundlePage(path)) return null;

        String id = null;
        for (String param : PRODUCT_PARAMS) {
            id = productIdIn(uri.getQueryParameter(param));
            if (id != null) break;
        }
        if (id == null) id = productIdIn(uri.toString());
        if (id == null) return null; // A bundle landing page with no product: leave it alone.

        return "https://" + host + "/item/" + id + ".html";
    }

    private static boolean isAliExpressHost(String host) {
        String h = host.toLowerCase(Locale.ROOT);
        return h.equals("aliexpress.com") || h.endsWith(".aliexpress.com")
                || h.equals("aliexpress.us") || h.endsWith(".aliexpress.us")
                || h.equals("aliexpress.ru") || h.endsWith(".aliexpress.ru");
    }

    private static boolean isBundlePage(String path) {
        return path.toLowerCase(Locale.ROOT).contains("bundledeals") || path.startsWith("/ssr/300000512/");
    }

    private static String productIdIn(String value) {
        if (value == null) return null;
        Matcher m = DIGITS.matcher(value);
        while (m.find()) {
            String candidate = m.group();
            if (PRODUCT_ID.matcher(candidate).matches()) return candidate;
        }
        return null;
    }
}
