package org.zankio.cculife;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import org.zankio.ccudata.base.source.http.HTTPSource;
import org.zankio.cculife.override.Net;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class CCULifeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        TrustManager[] trustManagers = Net.generateTrustManagers(this);
        if (trustManagers != null && trustManagers.length > 0
                && trustManagers[0] instanceof X509TrustManager)
        HTTPSource.trustManager = (X509TrustManager) trustManagers[0];
        HTTPSource.sslSocketFactory = Net.generateSSLSocketFactory(trustManagers);
    }
}
