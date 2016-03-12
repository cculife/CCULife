package org.zankio.cculife.ui.CourseSchedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.widget.ArrayAdapter;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.kiki.model.TimeTable;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;

public class CourseTimeTableActivity extends BaseFragmentActivity
        implements ActionBar.OnNavigationListener,
        IGetTimeTableData,
        IGetListener<TimeTable>,
        IGetInteger {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_timetable);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

            actionBar.setListNavigationCallbacks(
                    new ArrayAdapter<>(
                            getActionBarThemedContextCompat(),
                            android.R.layout.simple_list_item_1,
                            android.R.id.text1,
                            new String[]{
                                    getString(R.string.title_timetable_day),
                                    getString(R.string.title_timetable_week)
                            }),
                    this);
        }


        setMessageView(R.id.container);
        loading = false;
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
        getMenuInflater().inflate(R.menu.course_schedule, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        Fragment fragment;
        Bundle args = new Bundle();
        if(position == 0)
            fragment = new TimeTableDaysFragment();
        else // if (position == 1)
            fragment = new TimeTableWeekFragment();

        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }

    @Override
    public boolean getTimeTable(IOnUpdateListener<TimeTable> listener) {
        TimetableDataFragment dataFragment;
        dataFragment = TimetableDataFragment.getFragment(getSupportFragmentManager());
        dataFragment.init(this);

        return  dataFragment.getTimeTable(listener);
    }

    public IOnUpdateListener<TimeTable> getUpdateListener() {
        return TimetableDataFragment.getFragment(getSupportFragmentManager());

    }

    @Override
    public void registerListener(IOnUpdateListener<TimeTable> listener) {
        TimetableDataFragment.getFragment(getSupportFragmentManager()).registerListener(listener);
    }

    @Override
    public void unregisterListener(IOnUpdateListener<TimeTable> listener) {
        if (isFinishing()) return;
        TimetableDataFragment fragment = TimetableDataFragment.getFragment(getSupportFragmentManager(), true);
        if (fragment != null) fragment.unregisterListener(listener);
    }

    @Override
    public int getInt(String key) {
        return TimetableDataFragment.getFragment(getSupportFragmentManager()).getInt(key);
    }

    @Override
    public void setInt(String key, int value) {
        TimetableDataFragment.getFragment(getSupportFragmentManager()).setInt(key, value);
    }

}
