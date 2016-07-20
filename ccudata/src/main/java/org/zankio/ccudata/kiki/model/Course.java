package org.zankio.ccudata.kiki.model;

public class Course {
    public String CourseID;
    public String ClassID;
    public String Name;
    public String Teacher;
    public int Credit;
    public String CreditType;
    public String Time;
    public String ClassRoom;
    public String OutlineLink;
    public String Dept;
    public int term;
    public int year;

    //TODO
    public String getEcourseID() {
/*        if ("".equals(OutlineLink)) return null;
        if (year > 0 && term > 0)
            OutlineLink = OutlineLink
                    .replaceAll("([\\?&])year=\\d+", "$1year=" + year)
                    .replaceAll("([\\?&])term=\\d+", "$1term=" + term)
                    .replaceAll("^http://", "https://");

        Connection connection;
        String location, result;

        connection = Jsoup.connect(OutlineLink).timeout(Net.CONNECT_TIMEOUT);
        connection.followRedirects(false);

        try {
            location = connection.execute().header("Location");
            if (location != null) {
                result = Uri.parse(location).getQueryParameter("courseid");
                if (result != null)
                    return String.format(Locale.US, "%d_%d_%s", year, term, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return null;
    }

    public static class Time {
        public static Time prase(String data) {
            return null;
        }
    }
}
