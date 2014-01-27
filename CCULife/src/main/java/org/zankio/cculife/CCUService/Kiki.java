package org.zankio.cculife.CCUService;

import android.content.Context;
import android.net.Uri;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.CCUService.Parser.KikiParser;
import org.zankio.cculife.CCUService.Source.KikiRemoteSource;
import org.zankio.cculife.CCUService.Source.KikiSource;
import org.zankio.cculife.CCUService.SourceSwitcher.ISwitcher;
import org.zankio.cculife.CCUService.SourceSwitcher.SingleSourceSwitcher;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class Kiki {

    public ISwitcher sourceSwitcher;

    public Kiki(Context context) throws Exception {
        KikiRemoteSource kikiRemoteSource = new KikiRemoteSource(this, new KikiParser());
        kikiRemoteSource.Authenticate(SessionManager.getInstance(context));
        sourceSwitcher = new SingleSourceSwitcher(kikiRemoteSource);

    }

    public KikiSource getSource() {
        return (KikiSource) sourceSwitcher.getSource();
    }

    public TimeTable getTimeTable() throws Exception {
        return getSource().getTimeTable();
    }

    //ToDo Day-Class to Class-Day

    public class TimeTable {

        public Day[] days;

        public TimeTable() {
            days = new Day[7];
            for (int i = 0; i < 7; i++) {
                days[i] = new Day();
            }
        }

        public class Day {
            public ArrayList<Class> classList;

            public Day() {
                classList = new ArrayList<Class>();
            }
        }

        public class Class {
            public String name;
            public String classroom;
            public String teacher;
            public Calendar start;
            public Calendar end;
            public int color;
        }

        public void sort() {
            Comparator<Class> comparator = new Comparator<Class>() {
                public int compare(Class a, Class b) {
                    return a.start.compareTo(b.start);
                }
            };

            for (int i = 0; i < 7; i++) {
                Collections.sort(days[i].classList, comparator);
            }
        }
    }

    public Ecourse getEcourseCourse(int year, int term, org.zankio.cculife.CCUService.Ecourse ecourse) throws Exception {
        Course[] courses = getCourseList(year, term);

        return null;
    }

    public static class Course {
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
                        .replaceAll("([\\?&])term=\\d+", "$1term=" + term);

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
    public Course[] getCourseList() throws Exception {
        return getCourseList(-1, -1);
    }
    public Course[] getCourseList(int year, int term) throws Exception {
        return getSource().getCourseList(year, term);
    }
}
