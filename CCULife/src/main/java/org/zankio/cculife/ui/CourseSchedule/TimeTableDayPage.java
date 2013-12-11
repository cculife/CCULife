package org.zankio.cculife.ui.CourseSchedule;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.Kiki;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.Base.BasePage;
import org.zankio.cculife.ui.Base.onDataLoadListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TimeTableDayPage extends BasePage implements onDataLoadListener<Kiki.TimeTable> {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private Kiki.TimeTable timeTable;
    private ListView[] list;
    private ViewPager mViewPager;
    private TimeTableAdapter[] adapter;
    private boolean inited;

    public TimeTableDayPage(LayoutInflater inflater, FragmentManager fm, Kiki.TimeTable timeTable) {
        super(inflater);
        this.timeTable = timeTable;

        adapter = new TimeTableAdapter[7];
        list = new ListView[7];

    }

    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_timetable_daypage, null);
    }

    @Override
    public void initViews() {

        mSectionsPagerAdapter = new SectionsPagerAdapter();

        mViewPager = (ViewPager) PageView.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        updateTimeTable();
    }



    private void updateTimeTable() {
        for (int i = 1; i <= 5; i++) {
            if(adapter[i] != null)
                adapter[i].setClass(getWeekClasses(i));
        }

        if (!inited && timeTable != null) {
            int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
            inited = true;

            if(week < 5 && week >= 0)
                mViewPager.setCurrentItem(week);
        }
    }

    private ArrayList<Kiki.TimeTable.Class> getWeekClasses(int week) {
        if(timeTable == null || timeTable.days.length <= week || timeTable.days[week] == null) return null;
        return timeTable.days[week].classList;
    }

    @Override
    public void onDataLoaded(Kiki.TimeTable data) {
        timeTable = data;
        updateTimeTable();
    }

    public class SectionsPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem (ViewGroup container, int position) {
            int week = position + 1;
            View view = inflater.inflate(R.layout.fragment_course_timetable_day, null);

            adapter[week] = new TimeTableAdapter(getWeekClasses(week));
            list[week] = (ListView) view.findViewById(R.id.list);
            list[week].setAdapter(adapter[week]);

            container.addView(view, 0);
            return view;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "星期一";
                case 1:
                    return "星期二";
                case 2:
                    return "星期三";
                case 3:
                    return "星期四";
                case 4:
                    return "星期五";
            }
            return null;
        }
    }

    public class TimeTableAdapter extends BaseAdapter {

        private ArrayList<Kiki.TimeTable.Class> classes;

        public TimeTableAdapter(ArrayList<Kiki.TimeTable.Class> classes) {
            this.classes = classes;
        }

        public void setClass(ArrayList<Kiki.TimeTable.Class> classes){
            this.classes = classes;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return classes == null ? 0 : classes.size();
        }

        @Override
        public Object getItem(int position) {
            return classes == null ? null : classes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Kiki.TimeTable.Class course = (Kiki.TimeTable.Class) getItem(position);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

            View view = inflater.inflate(R.layout.item_timetable_day, null);
            ((TextView)view.findViewById(R.id.CourseName)).setText(course.name);
            ((TextView)view.findViewById(R.id.ClassRoom)).setText(course.classroom);
            ((TextView)view.findViewById(R.id.CourseTime)).setText(
                    simpleDateFormat.format(course.start.getTime()) + "-" + simpleDateFormat.format(course.end.getTime())
            );

            return view;
        }
    }

}
