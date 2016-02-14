package org.zankio.cculife.CCUService.kiki.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

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
            classList = new ArrayList<>();
        }
    }

    public class Class {
        public String name;
        public String classroom;
        public String teacher;
        public Calendar start;
        public Calendar end;
        public int color;
        public int colorid;
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
