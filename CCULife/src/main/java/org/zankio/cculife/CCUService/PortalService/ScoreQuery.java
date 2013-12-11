package org.zankio.cculife.CCUService.PortalService;

public class ScoreQuery extends BasePortal{

    {
        PORTAL_ID = "0007";
        PORTAL_URL = "http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/library/SSO/Query_grade/getssoCcuRight.php";
    }

    @Override
    public String[] onPostExcute(String... param) {
        String[] result = new String[param.length + 1];

        System.arraycopy(param, 0, result, 0, param.length);

        for (int i = 0; i < param.length; i++) {
            result[i].replace("140.123.30.107", "kiki.ccu.edu.tw");
        }

        result[param.length] = "http://kiki.ccu.edu.tw/~ccmisp06/cgi-bin/Query/Query_grade.php";
        return result;
    }
}
