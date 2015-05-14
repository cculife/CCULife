package org.zankio.cculife.CCUService.portal.service;

public class Ecourse extends BasePortal {

    public static final String LOGIN_COURSE_URL = "http://ecourse.ccu.edu.tw/php/login_s.php?courseid=%s";
    protected String CourseID;

    {
        CourseID = null;
        PORTAL_ID = "0000";
        PORTAL_URL = "http://ecourse.ccu.edu.tw/php/getssoCcuRight.php";
    }

    public Ecourse setCourseID(String ID) {
        CourseID = ID;
        return this;
    }


    @Override
    public String[] onPostExcute(String... param) {
        String[] result = param;

        if (CourseID != null) {
            result = new String[param.length + 1];
            System.arraycopy(param, 0, result, 0, param.length);

            result[param.length] = String.format(LOGIN_COURSE_URL, CourseID);
        }
        return result;
    }
}
