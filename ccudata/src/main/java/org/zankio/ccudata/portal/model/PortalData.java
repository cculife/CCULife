package org.zankio.ccudata.portal.model;

import rx.Observable;

public class PortalData {
    private static final String SSO_URL_BASE = "http://portal.ccu.edu.tw/ssoService.php?service=%s&linkId=%s";
    public final String PORTAL_URL;
    public final String PORTAL_ID;

    public PortalData(String portal_url, String portal_id) {
        PORTAL_URL = portal_url;
        PORTAL_ID = portal_id;
    }

    public String getSSOPortalURL() {
        return String.format(SSO_URL_BASE, PORTAL_URL, PORTAL_ID);
    }

    public Observable.Transformer<String, String> after() {
        return stringObservable -> stringObservable;
    }
}
