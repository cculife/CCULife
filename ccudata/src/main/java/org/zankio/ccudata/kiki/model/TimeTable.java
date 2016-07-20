package org.zankio.ccudata.kiki.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

public class TimeTable {

    public Day[] days;
    public void mergeTimetable(TimeTable timeTable) {
        for (int i = 0; i < timeTable.days.length; i++) {
            for (Class mClass : timeTable.days[i].classList) {
                if (days[i].classList.indexOf(mClass) < 0)
                    days[i].classList.add(mClass);
            }
        }
        this.sort();
    }

    public TimeTable() {
        days = new Day[7];
        for (int i = 0; i < 7; i++) {
            days[i] = new Day();
        }
    }

    public void remove(Class course) {
        for (Day day: days) {
            for (int i = 0; i < day.classList.size(); i++) {
                Class c = day.classList.get(i);
                if (c.course_id.equals(course.course_id)) {
                    day.classList.remove(c);
                    i--;
                }
            }
        }
    }

    public boolean exist(String course_id) {
        for (Day day: days) {
            for (Class c: day.classList) {
                if (c.course_id.equals(course_id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public class Day {
        public ArrayList<Class> classList;

        public Day() {
            classList = new ArrayList<>();
        }
    }

    public class Class {
        public String course_id;
        public String name;
        public String classroom;
        public String teacher;
        public Calendar start;
        public Calendar end;
        public int userAdd;
        public int color;
        public int colorid;

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (getClass() != o.getClass()) return false;

            final Class mClass = (Class) o;
            if (!_equals(course_id, mClass.course_id)) return false;
            if (!_equals(name, mClass.name)) return false;
            if (!_equals(classroom, mClass.classroom)) return false;
            if (!_equals(teacher, mClass.teacher)) return false;
            if (!_equals(start, mClass.start)) return false;
            if (!_equals(end, mClass.end)) return false;
            //if (!_equals(userAdd, mClass.userAdd)) return false;
            return true;
        }

        public boolean _equals(Object a, Object b) {
            return (a == null) ? (b == null) : a.equals(b);
        }

        public Class() {}
        public Class(Class mClass) {
            this.course_id = mClass.course_id;
            this.name = mClass.name;
            this.teacher = mClass.teacher;
            this.classroom = mClass.classroom;
            this.start = mClass.start;
            this.end = mClass.end;
            this.color = mClass.color;
            this.colorid = mClass.colorid;
            this.userAdd = mClass.userAdd;
        }
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
