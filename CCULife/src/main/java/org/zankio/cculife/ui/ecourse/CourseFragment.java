package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.ui.base.IGetCourseData;

import java.util.Locale;

public class CourseFragment extends Fragment implements ActionBar.TabListener {

    private static Page[] pages = new Page[0];

    //ToDo Don't reload on rotation.
    CoursePagerAdapter mSectionsPagerAdapter;
    protected Course course;
    IGetCourseData courseDataContext;
    ViewPager mViewPager;
    ActionBar actionBar;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            courseDataContext = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetCourseData");
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        pages = new Page[]{
                new Page(R.string.title_announce, new CourseAnnounceFragment()),
                new Page(R.string.title_score, new CourseScoreFragment()),
                new Page(R.string.title_file, new CourseFileFragment()),
                new Page(R.string.title_homework, new CourseHomeworkFragment()),
                new Page(R.string.title_named, new CourseRollCallFragment()),
        };

        mSectionsPagerAdapter = new CoursePagerAdapter(getFragmentManager());

        String id = getArguments().getString("id");
        this.course = courseDataContext.getCourse(id);
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setTitle(course.name);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        for (Page page : pages) {
            Bundle data = new Bundle();
            data.putString("id", course.courseid);
            page.fragment.setArguments(data);
        }

        mSectionsPagerAdapter.notifyDataSetChanged();

        actionBar.removeAllTabs();
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        ((BaseFragmentActivity)getActivity()).setSSOService(new org.zankio.cculife.CCUService.portal.service.Ecourse().setCourseID(course.courseid));
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_course, container, false);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setTitle("Course");
        }

        for (Page page : pages) {
           getFragmentManager().beginTransaction().remove(page.fragment).commit();
        }
    }

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        if (CourseListFragment.ecourse == null) {finish(); return;}
        course = CourseListFragment.ecourse.nowCourse;
    }*/

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()){
            case R.id.action_classmate:
                CourseClassmateActivity.course = course;

                intent = new Intent(getContext(), CourseClassmateActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        if (mViewPager != null) mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) { }
    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) { }

    public class CoursePagerAdapter extends FragmentPagerAdapter {

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            return pages[position].fragment;
        }

        @Override
        public int getCount() {
            return pages.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            if (position < pages.length)
                return getString(pages[position].title).toUpperCase(l);
            return null;
        }
    }

    public static class Page {
        public Fragment fragment;
        public int title;
        public Page (int title, Fragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }

}
