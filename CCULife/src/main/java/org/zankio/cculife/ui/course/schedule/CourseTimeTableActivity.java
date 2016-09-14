package org.zankio.cculife.ui.course.schedule;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.zankio.ccudata.base.model.Storage;
import org.zankio.ccudata.kiki.model.TimeTable;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.ui.base.GetStorage;

import rx.subjects.ReplaySubject;

public class CourseTimeTableActivity extends BaseFragmentActivity
        implements IGetTimeTableData, GetStorage, AdapterView.OnItemSelectedListener {

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private Spinner toolbarSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_timetable);

        // initial Action bar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        toolbarSpinner = (Spinner) findViewById(R.id.toolbar_spinner);
        toolbarSpinner.setAdapter(
                new ArrayAdapter<>(
                        getActionBarThemedContextCompat(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_timetable_day),
                                getString(R.string.title_timetable_week)
                        })
        );
        toolbarSpinner.setOnItemSelectedListener(this);

        // set message view
        message().content(R.id.container);
    }

    private Context getActionBarThemedContextCompat() {
       return getSupportActionBar().getThemedContext();
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // restore select
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            toolbarSpinner.setSelection(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                toolbarSpinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    ////// Option Menu //////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_schedule, menu);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Fragment fragment;
        Bundle args = new Bundle();
        if(i == 0)
            fragment = new TimeTableDaysFragment();
        else // if (position == 1)
            fragment = new TimeTableWeekFragment();

        fragment.setArguments(args);

        if (!isFinishing())
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commitAllowingStateLoss();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    ////// TimeTable Data //////
    @Override
    public ReplaySubject<TimeTable> getTimeTable() {
        TimetableDataFragment dataFragment;
        dataFragment = TimetableDataFragment.getFragment(getSupportFragmentManager());
        dataFragment.init(this);

        return dataFragment.getTimeTable();
    }

    ////// storage data in fragment //////
    @Override
    public Storage storage() {
        TimetableDataFragment dataFragment = TimetableDataFragment.getFragment(getSupportFragmentManager());
        dataFragment.init(this);
        return dataFragment.storage();
    }
}
