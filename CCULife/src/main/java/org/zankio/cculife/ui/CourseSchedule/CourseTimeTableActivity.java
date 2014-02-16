package org.zankio.cculife.ui.CourseSchedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;

import org.zankio.cculife.CCUService.Kiki;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BaseFragmentActivity;

public class CourseTimeTableActivity extends BaseFragmentActivity implements ActionBar.OnNavigationListener {

    //ToDo Don't reload on rotation.
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    public static Kiki.TimeTable timeTable;
    private Kiki courseTimeTable;
    private static TimeTableWeekPage timeTableWeekPage;
    private static TimeTableDayPage timeTableDayPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_timetable);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        actionBar.setListNavigationCallbacks(
                new ArrayAdapter<String>(
                        getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_timetable_day),
                                getString(R.string.title_timetable_week)
                        }),
                this);


        setMessageView(R.id.container);

        new LoadTimeTableDataAsyncTask().execute();
    }

    private Context getActionBarThemedContextCompat() {
       return getSupportActionBar().getThemedContext();
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getSupportActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.course_schedule, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        Fragment fragment = new DummySectionFragment();
        Bundle args = new Bundle();
        if(position == 0)
            args.putInt(DummySectionFragment.ARG_PAGE_VIEW, R.layout.fragment_course_timetable_day);
        else if(position == 1)
            args.putInt(DummySectionFragment.ARG_PAGE_VIEW, R.layout.fragment_course_timetable_week);

        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }


    public static class DummySectionFragment extends Fragment {
        private static final String ARG_PAGE_VIEW = "pageview";

        public DummySectionFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int layout = getArguments().getInt(ARG_PAGE_VIEW);

            switch (layout) {
                case R.layout.fragment_course_timetable_week:
                    return (timeTableWeekPage = new TimeTableWeekPage(inflater, timeTable)).getView();
                case R.layout.fragment_course_timetable_day:
                    return (timeTableDayPage = new TimeTableDayPage(inflater, timeTable)).getView();
            }
            return inflater.inflate(layout, null);
        }
    }

    public class LoadTimeTableDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Kiki.TimeTable> {

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessage("讀取中...", true);
        }

        @Override
        protected Kiki.TimeTable _doInBackground(Void... params) throws Exception{
            if(courseTimeTable == null) courseTimeTable = new Kiki(CourseTimeTableActivity.this);
            return courseTimeTable.getTimeTable();
        }

        @Override
        protected void _onPostExecute(Kiki.TimeTable timeTable) {
            if (timeTable == null) {
                showMessage("沒有課表");
                return;
            }
            CourseTimeTableActivity.this.timeTable = timeTable;
            onDataLoad();
            hideMessage();
        }
    }

    private void onDataLoad() {
        if(timeTableWeekPage != null) timeTableWeekPage.onDataLoaded(timeTable);
        if(timeTableDayPage != null) timeTableDayPage.onDataLoaded(timeTable);
    }
}
