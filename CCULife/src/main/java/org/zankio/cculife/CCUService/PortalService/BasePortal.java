package org.zankio.cculife.CCUService.PortalService;

public abstract class BasePortal {
    private static final String SSO_URL_BASE = "http://portal.ccu.edu.tw/ssoService.php?service=%s&linkId=%s";
    public String PORTAL_URL = null;
    public String PORTAL_ID = null;

    public String getSSOPortalURL() {
        return String.format(SSO_URL_BASE, PORTAL_URL, PORTAL_ID);
    }

    public String[] onPostExcute(String... param) {
        return param;
    }
}
