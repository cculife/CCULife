package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.RollCall;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Subscriber;


public class CourseRollCallFragment extends BaseMessageFragment implements IGetLoading{
    private IGetCourseData context;
    private RollCallAdapter adapter;
    private Course course;
    private ListView list;
    private boolean loading;
    private boolean loaded;
    private CourseFragment.LoadingListener loadedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IGetCourseData");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_rollcall, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new RollCallAdapter();
        list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        courseChange(getArguments().getString("id"));
    }

    public void courseChange(String id) {
        course = context.getCourse(id);
        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        course.getRollCall().subscribe(new Subscriber<Response<RollCall[], CourseData>>() {
            @Override
            public void onCompleted() {
                setLoaded(true);
            }

            @Override
            public void onError(Throwable e) {
                e = ExceptionUtils.extraceException(e);

                CourseRollCallFragment.this.loading = false;
                setLoaded(true);
                message().show(e.getMessage());
            }

            @Override
            public void onNext(Response<RollCall[], CourseData> courseDataResponse) {
                CourseRollCallFragment.this.loading = false;

                RollCall[] rollCalls = courseDataResponse.data();
                if (rollCalls == null || rollCalls.length == 0) {
                    message().show("沒有點名");
                    return;
                }

                adapter.setRollCall(rollCalls);

                message().hide();

            }

            @Override
            public void onStart() {
                super.onStart();

                setLoaded(false);
                message().show("讀取中...", true);
            }
        });
    }


    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
        if (loadedListener != null) loadedListener.call(loaded);
    }

    @Override
    public boolean isLoading() {
        return !this.loaded;
    }

    @Override
    public void setLoadedListener(CourseFragment.LoadingListener listener) {
        this.loadedListener = listener;
    }

    public class RollCallAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private RollCall[] rollcalls;

        public RollCallAdapter() {
            this.inflater = LayoutInflater.from(getContext());
        }

        public void setRollCall(RollCall[] rollCall){
            this.rollcalls = rollCall;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return rollcalls == null ? 0 : rollcalls.length;
        }

        @Override
        public Object getItem(int position) {
            return rollcalls == null ? null : rollcalls[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) view = inflater.inflate(R.layout.item_rollcall, parent, false);
            else view = convertView;

            RollCall rollcall = (RollCall) getItem(position);

            ((TextView)view.findViewById(R.id.Date)).setText(rollcall.date);
            ((TextView)view.findViewById(R.id.Comment)).setText(rollcall.comment);

            return view;
        }
    }
}
