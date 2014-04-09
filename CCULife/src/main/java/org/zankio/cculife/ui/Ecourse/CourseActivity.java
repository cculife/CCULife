package org.zankio.cculife.ui.Ecourse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.Base.BaseFragmentActivity;

import java.util.Locale;

public class CourseActivity extends BaseFragmentActivity implements ActionBar.TabListener {

    //ToDo Don't reload on rotation.
    SectionsPagerAdapter mSectionsPagerAdapter;
    protected Ecourse.Course course;
    protected Ecourse ecourse;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        if (CourseListActivity.ecourse == null) {finish(); return;}
        course = CourseListActivity.ecourse.nowCourse;
        ecourse = course.getEcourse();

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(course.getName());

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        setSSOService(new org.zankio.cculife.CCUService.PortalService.Ecourse().setCourseID(course.getCourseid()));
    }

    @Override
    protected void onPause() {
        if(ecourse != null) ecourse.closeSource();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(ecourse != null) ecourse.openSource();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.course, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){
            case R.id.action_classmate:
                intent = new Intent(this, CourseClassmateActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //Todo ?
            DummySectionFragment.course = course;

            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            if(position == 0)
                args.putInt(DummySectionFragment.ARG_PAGE_VIEW, R.layout.fragment_course_announce);
            else if(position == 1)
                args.putInt(DummySectionFragment.ARG_PAGE_VIEW, R.layout.fragment_course_score);
            else if(position == 2)
                args.putInt(DummySectionFragment.ARG_PAGE_VIEW, R.layout.fragment_course_file);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_announce).toUpperCase(l);
                case 1:
                    return getString(R.string.title_score).toUpperCase(l);
                case 2:
                    return getString(R.string.title_file).toUpperCase(l);
            }
            return null;
        }
    }

    public static class DummySectionFragment extends Fragment {
        public static final String ARG_PAGE_VIEW = "pageview";
        private static Ecourse.Course course;

        public DummySectionFragment() {
        }

        /*public DummySectionFragment(Ecourse.Course course){
            this.course = course;
        }*/

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int layout = getArguments().getInt(ARG_PAGE_VIEW);

            switch (layout) {
                case R.layout.fragment_course_announce:
                    return new CourseAnnouncePage(inflater, course).getView();
                case R.layout.fragment_course_score:
                    return new CourseScorePage(inflater, course).getView();
                case R.layout.fragment_course_file:
                    return new CourseFilePage(inflater, course).getView();
            }
            return inflater.inflate(layout, null);
        }
    }

}
