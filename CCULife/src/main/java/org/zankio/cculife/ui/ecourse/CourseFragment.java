package org.zankio.cculife.ui.ecourse;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.portal.model.EcoursePortalData;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.ui.base.IGetCourseData;

import java.util.Locale;

import rx.functions.Action1;

public class CourseFragment extends Fragment {
    interface LoadingListener extends Action1<Boolean> {}

    private Page[] pages;// = new Page[0];

    CoursePagerAdapter mPagerAdapter;
    protected Course course;
    private IGetCourseData context;
    private ValueAnimator loadingAnimator;
    private ValueAnimator transAnimator;
    private int tab_loading_form;
    private int tab_loading_to;
    ViewPager mViewPager;
    ActionBar actionBar;
    TabLayout tabLayout;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IGetCourseData");
        }
        tab_loading_form = ContextCompat.getColor(context, R.color.tab_loading_from);
        tab_loading_to = ContextCompat.getColor(context, R.color.tab_loading_to);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pages = new Page[]{
                new Page(R.string.title_announce, new CourseAnnounceFragment()),
                new Page(R.string.title_score, new CourseScoreFragment()),
                new Page(R.string.title_file, new CourseFileFragment()),
                new Page(R.string.title_homework, new CourseHomeworkFragment()),
                new Page(R.string.title_roll_call, new CourseRollCallFragment()),
        };

        String id = getArguments().getString("id");
        this.course = context.getCourse(id);

        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        for (Page page : pages) {
            Bundle data = new Bundle();
            data.putString("id", course.courseid);
            page.fragment.setArguments(data);
        }

        mPagerAdapter = new CoursePagerAdapter(getChildFragmentManager());
        mPagerAdapter.notifyDataSetChanged();

        ((BaseFragmentActivity)getActivity()).setSSOService(new EcoursePortalData().setCourseID(course.courseid));

        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(course.name);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        tabLayout = (TabLayout) view.findViewById(R.id.tab);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mViewPager.addOnPageChangeListener(pageChangeListener);
        }

        pageChangeListener.onPageSelected(0);
    }

    private final ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            IGetLoading fragment = (IGetLoading) mPagerAdapter.getItem(position);
            fragment.setLoadedListener(loadedListener);
            setLoading(fragment.isLoading());
        }
    };

    private final LoadingListener loadedListener = (loaded) -> setLoading(!loaded);

    private void setLoading(boolean loading) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (loadingAnimator == null) {
                loadingAnimator = ObjectAnimator.ofInt(
                        tabLayout,
                        "selectedTabIndicatorColor",
                        tab_loading_form,
                        tab_loading_to
                );

                loadingAnimator.setEvaluator(new ArgbEvaluator());
                loadingAnimator.setDuration(1500);
                loadingAnimator.setRepeatCount(ValueAnimator.INFINITE);
                loadingAnimator.setRepeatMode(ValueAnimator.REVERSE);
            }

            boolean running = loadingAnimator.isRunning();

            if (running && !loading) {
                loadingAnimator.cancel();

                transAnimator = ObjectAnimator.ofInt(
                        tabLayout,
                        "selectedTabIndicatorColor",
                        tab_loading_to,
                        tab_loading_form
                );
                long currentPlayTime = loadingAnimator.getCurrentPlayTime();
                transAnimator.setEvaluator(new ArgbEvaluator());
                transAnimator.setCurrentPlayTime(currentPlayTime);
                transAnimator.setDuration(500);
                transAnimator.start();
            } else if (!running && loading) {
                loadingAnimator.start();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (Page p : pages) {
            p.fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Course");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            /*case R.id.action_classmate:
                Fragment fragment = new CourseClassmateFragment();
                Bundle data = new Bundle();
                data.putString("id", course.courseid);
                fragment.setArguments(data);

                FragmentManager fm = getFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.container, fragment)
                        .addToBackStack(null)
                        .commit();

                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    public class CoursePagerAdapter extends FragmentPagerAdapter {

        public CoursePagerAdapter(FragmentManager fm) {
            super(fm);
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
