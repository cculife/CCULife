package org.zankio.cculife.CCUService.kiki.model;

import android.net.Uri;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.override.Net;

import java.io.IOException;

public class Course {
    public String CourseID;
    public String ClassID;
    public String Name;
    public String Teacher;
    public int Credit;
    public String CreditType;
    public Time[] Time;
    public String ClassRoom;
    public String OutlineLink;
    public int term;
    public int year;

    public String getEcourseID() {
        if ("".equals(OutlineLink)) return null;
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
                    return String.format("%d_%d_%s", year, term, result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Time {
        public static Time prase(String data) {
            return null;
        }
    }
}
