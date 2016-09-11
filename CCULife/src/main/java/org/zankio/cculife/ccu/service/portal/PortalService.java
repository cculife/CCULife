package org.zankio.cculife.ccu.service.portal;

import android.app.Activity;
import android.widget.Toast;

import org.zankio.ccudata.portal.Portal;
import org.zankio.ccudata.portal.model.PortalData;
import org.zankio.ccudata.portal.source.SSOLoginSource;
import org.zankio.cculife.utils.BrowserUtils;

import rx.Observable;


public class PortalService {
    public static void openPortal(Activity activity, Portal portal, PortalData data) {
        Toast.makeText(activity, "請稍候...", Toast.LENGTH_SHORT).show();

        portal
                .fetch(SSOLoginSource.request(data))
                .subscribe(
                        response -> {
                            String url = response.data();

                            Observable.just(url)
                                    .compose(data.after())
                                    .toList()
                                    .subscribe(strings ->
                                                    BrowserUtils.open(
                                                            activity,
                                                            strings.toArray(new String[strings.size()])
                                                    ),
                                            throwable -> {
                                                Toast.makeText(activity, "使用瀏覽器開啟錯誤", Toast.LENGTH_SHORT).show();
                                                throwable.printStackTrace();
                                            }
                                    );
                        },
                        throwable -> {
                            Toast.makeText(activity, "使用瀏覽器開啟錯誤", Toast.LENGTH_SHORT).show();
                            throwable.printStackTrace();
                        }
                );
    }
}
