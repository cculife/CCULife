package org.zankio.cculife.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;

import org.zankio.cculife.R;

public class BrowserUtils {

    public static void open(Activity activity, String url) {
        // if sdk version below ics
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            openInBrowser(activity, url);
            return;
        }

        // open custom tabs
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(activity, R.color.accent));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    private static void openInBrowser(Activity activity, String ...urls) {
        int delay = 0;
        Handler handler = new Handler();

        // open urls with delay
        for (String url : urls) {
            handler.postDelayed(() -> activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))), delay);
            delay += 1000;
        }
    }

    public static void open(Activity activity, String ...urls) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            openInBrowser(activity, urls);
            return;
        }

        final String CUSTOM_TAB_PACKAGE_NAME = "com.android.chrome";  // Change when in stable
        CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
            public CustomTabsClient mCustomTabsClient;
            private CustomTabsSession session = null;
            private int index = 0;

            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                mCustomTabsClient = client;

                session = client.newSession(new CustomTabsCallback() {
                    @Override
                    public void onNavigationEvent(int navigationEvent, Bundle extras) {
                        super.onNavigationEvent(navigationEvent, extras);

                        if (navigationEvent == NAVIGATION_FINISHED && index < urls.length) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);
                            builder.setToolbarColor(ContextCompat.getColor(activity, R.color.accent));
                            CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(activity, Uri.parse(urls[index++]));
                        }
                    }
                });

                if (index < urls.length) {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);
                    builder.setToolbarColor(ContextCompat.getColor(activity, R.color.accent));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(activity, Uri.parse(urls[index++]));
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        boolean ok = CustomTabsClient.bindCustomTabsService(activity, CUSTOM_TAB_PACKAGE_NAME, connection);

        if (!ok) openInBrowser(activity, urls);

    }
}
