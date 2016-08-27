package org.zankio.cculife.ccu.service.portal;

import android.app.Activity;

import org.zankio.ccudata.portal.Portal;
import org.zankio.ccudata.portal.model.PortalData;
import org.zankio.ccudata.portal.source.SSOLoginSource;
import org.zankio.cculife.utils.BrowserUtils;

import rx.Observable;


public class PortalService {
    public static void openPortal(Activity activity, Portal portal, PortalData data) {
        portal
                .fetch(SSOLoginSource.request(data))
                .subscribe(response -> {
                    String url = response.data();

                    Observable.just(url)
                            .compose(data.after())
                            .toList()
                            .subscribe(strings ->
                                    BrowserUtils.open(
                                            activity,
                                            strings.toArray(new String[strings.size()])
                                    )
                            );
                });
    }
}
