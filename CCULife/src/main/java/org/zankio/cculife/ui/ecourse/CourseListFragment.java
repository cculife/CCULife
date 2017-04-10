package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.utils.RxJavaUtils;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.source.remote.CourseListSource;
import org.zankio.ccudata.portal.model.EcoursePortalData;
import org.zankio.cculife.Debug;
import org.zankio.cculife.R;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.GetStorage;
import org.zankio.cculife.utils.ExceptionUtils;
import org.zankio.cculife.utils.SettingUtils;

import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class CourseListFragment extends BaseMessageFragment {

    private static final String CACHE_COURSE_LIST = "CACHE_COURSE_LIST";
    public static Ecourse ecourse;
    private CourseAdapter adapter = null;
    private OnCourseSelectedListener context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context = (OnCourseSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IGetCourseData");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void fetchCourseList() {
        Log.d("CourseListFragment", "fetch Course List");
        if (ecourse != null && adapter != null && adapter.getCount() != 0) {
            adapter.notifyDataSetChanged();
            return;
        }

        Context context = getContext();
        UserManager userManager = UserManager.getInstance(context);

        ecourse = new Ecourse(context);
        ecourse.setOfflineMode(SettingUtils.loadOffline(context))
               .user()
                   .username(userManager.getUsername())
                   .password(userManager.getPassword());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (Debug.debug && preferences.getBoolean("debug_ecourse_custom", false)) {
            int year, term;

            year = Integer.parseInt(preferences.getString("debug_ecourse_year", "-1"));
            term = Integer.parseInt(preferences.getString("debug_ecourse_term", "-1"));
            // TODO: 2016/7/22
            //ecourse.fetch(CustomCourseListSource.request(year, term, new Kiki(getContext()));
        } else {
            // TODO: 2016/8/7

            Observable<Response<Course[], CourseData>> observableCache;
            observableCache = ((GetStorage)context).storage().get(CACHE_COURSE_LIST);

            if (observableCache == null) {
                observableCache = ecourse.fetch(CourseListSource.request())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .compose(RxJavaUtils.cache());
                ((GetStorage)context).storage().put(CACHE_COURSE_LIST, observableCache);
            }

            observableCache.subscribe(new Subscriber<Response<Course[], CourseData>>() {
                private boolean noData = true;

                @Override
                public void onStart() {
                    super.onStart();
                    message().show("讀取中...", true);
                }

                @Override
                public void onCompleted() {
                    if (noData)
                        message().show("沒有課程");
                }

                @Override
                public void onError(Throwable e) {
                    e = ExceptionUtils.extraceException(e);

                    message().show(e.getMessage());
                }

                @Override
                public void onNext(Response<Course[], CourseData> courseDataResponse) {
                    Course[] courses = courseDataResponse.data();
                    if(courses == null || courses.length == 0) {
                        return;
                    }

                    adapter.setCourses(courses);
                    noData = false;
                    message().hide();
                }
            });
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courselist, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((BaseFragmentActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((BaseFragmentActivity)getActivity()).setSSOService(new EcoursePortalData());

        adapter = new CourseAdapter();
        ListView courselist = (ListView)view.findViewById(R.id.list);
        courselist.setAdapter(adapter);
        courselist.setOnItemClickListener((parent, view1, position, id) -> {
            final Course course = (Course) parent.getAdapter().getItem(position);
            context.onCourseSelected(ecourse, course);
        });

        ((BaseFragmentActivity)getActivity()).message().content(R.id.list);

        fetchCourseList();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.course_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public class CourseAdapter extends BaseAdapter {

        Course[] courses = null;
        private final boolean ignore_ecourse_warnning;
        public CourseAdapter() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            this.ignore_ecourse_warnning = preferences.getBoolean("ignore_ecourse_warnning", false);
        }

        public void setCourses(Course[] courses){
            this.courses = courses;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return courses == null ? 0 : courses.length;
        }

        @Override
        public Object getItem(int position) {
            return courses == null ? null : courses[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = convertView;

            if(view == null) {
                view = inflater.inflate(R.layout.item_course, parent, false);
            }

            Course course = courses[position];
            ((TextView) (view.findViewById(R.id.course_name))).setText(course.name);

            ((TextView)view.findViewById(R.id.unread)).setText(String.format(Locale.US, "%d", course.notice + course.homework + course.exam));

            if (ignore_ecourse_warnning) {
                int color = ContextCompat.getColor(getContext(), R.color.Red_Course_Warring);
                view.findViewById(R.id.warring).setBackgroundColor(course.warning ? color : 0);
            }

            return view;
        }
    }

    public interface OnCourseSelectedListener {
        void onCourseSelected(Ecourse ecourse, Course course);
    }
    
}
