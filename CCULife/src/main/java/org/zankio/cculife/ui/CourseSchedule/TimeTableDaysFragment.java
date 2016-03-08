package org.zankio.cculife.ui.CourseSchedule;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.kiki.model.TimeTable;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class TimeTableDaysFragment extends BaseMessageFragment
        implements IOnUpdateListener<TimeTable> {
    private IGetTimeTableData timeTableDataContext;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TimeTableAdapter[] adapter;
    private ListView[] list;
    private TimeTable timeTable;
    private boolean loading;
    private ViewPager mViewPager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            timeTableDataContext = (IGetTimeTableData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetCourseData");
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_timetable_daypage, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        adapter = new TimeTableAdapter[7];
        list = new ListView[7];

        this.loading = timeTableDataContext.getTimeTable(this);
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        this.loading = false;
        showMessage(err.getMessage());
    }

    @Override
    public void onNext(String type, TimeTable timeTable, BaseSource source) {
        this.loading = false;
        this.timeTable = timeTable;
        updateTimeTable();
    }

    @Override
    public void onComplete(String type) {

    }

    public class SectionsPagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;
        private View old;

        public SectionsPagerAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

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
            list[week].setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (old != null) old.findViewById(R.id.course_id).setSelected(false);
                    view.findViewById(R.id.course_id).setSelected(true);
                    old = view;
                }
            });
            container.addView(view, 0);
            return view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "星期一";
                case 1: return "星期二";
                case 2: return "星期三";
                case 3: return "星期四";
                case 4: return "星期五";
            }
            return null;
        }
    }

    private void updateTimeTable() {
        for (int i = 1; i <= 5; i++) {
            if(adapter[i] != null)
                adapter[i].setClass(getWeekClasses(i));
        }

        if (timeTable != null) {
            int week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;

            if(week < 5 && week >= 0)
                mViewPager.setCurrentItem(week);
        }
    }

    private ArrayList<TimeTable.Class> getWeekClasses(int week) {
        if(timeTable == null || timeTable.days.length <= week || timeTable.days[week] == null) return null;
        return timeTable.days[week].classList;
    }

    public class TimeTableAdapter extends BaseAdapter implements View.OnFocusChangeListener {

        private ArrayList<TimeTable.Class> classes;
        private LayoutInflater inflater;

        public TimeTableAdapter(ArrayList<TimeTable.Class> classes) {
            this.classes = classes;
            this.inflater = LayoutInflater.from(getActivity());
        }

        public void setClass(ArrayList<TimeTable.Class> classes){
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
            TimeTable.Class course = (TimeTable.Class) getItem(position);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);

            View view;
            if (convertView == null) view = inflater.inflate(R.layout.item_timetable_day, parent, false);
            else view = convertView;

            ((TextView)view.findViewById(R.id.name)).setText(course.name);
            ((TextView)view.findViewById(R.id.course_id)).setText(
                    String.format(
                            "%s / %s-%s / %s",
                            course.classroom,
                            simpleDateFormat.format(course.start.getTime()),
                            simpleDateFormat.format(course.end.getTime()),
                            course.teacher
                    )
            );

            view.setOnFocusChangeListener(this);
            return view;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            v.findViewById(R.id.course_id).setSelected(hasFocus);
        }
    }

}
