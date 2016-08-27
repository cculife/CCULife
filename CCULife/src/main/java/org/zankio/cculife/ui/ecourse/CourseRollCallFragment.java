package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
    private boolean loaded;
    private View statisticView;
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
        list.setDivider(null);

        statisticView = View.inflate(getContext(), R.layout.item_rollcall, null);
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

        course.getRollCall().subscribe(new Subscriber<Response<RollCall, CourseData>>() {
            private boolean noData = true;

            @Override
            public void onCompleted() {
                if (noData)
                    message().show("沒有點名");

                setLoaded(true);
            }

            @Override
            public void onError(Throwable e) {
                e = ExceptionUtils.extraceException(e);

                setLoaded(true);
                message().show(e.getMessage());
            }

            @Override
            public void onNext(Response<RollCall, CourseData> courseDataResponse) {
                RollCall rollCall = courseDataResponse.data();
                if (rollCall == null || rollCall.records == null || rollCall.records.length == 0) {
                    return;
                }

                adapter.setRollCall(rollCall);

                StringBuilder statistic = new StringBuilder();
                if (rollCall.attend >= 0)
                    statistic.append("出席: ").append(rollCall.attend);

                if (rollCall.absent >= 0) {
                    if (statistic.length() > 0) statistic.append("    ");
                    statistic.append("缺席: ").append(rollCall.absent);
                }
                ((TextView)statisticView.findViewById(R.id.Date)).setText("統計");
                ((TextView)statisticView.findViewById(R.id.Comment)).setText(statistic.toString());
                //list.removeFooterView(statisticView);
                list.addFooterView(statisticView);
                list.setAdapter(adapter);

                noData = false;
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
        private RollCall rollcall;

        public void setRollCall(RollCall rollCall){
            this.rollcall = rollCall;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return rollcall == null ? 0 : rollcall.records.length;
        }

        @Override
        public Object getItem(int position) {
            return rollcall == null ? null : rollcall.records[position];
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
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (convertView == null)
                convertView = inflater.inflate(R.layout.item_rollcall, parent, false);

            RollCall.Record rollcall = (RollCall.Record) getItem(position);

            ((TextView)convertView.findViewById(R.id.Date)).setText(rollcall.date);
            ((TextView)convertView.findViewById(R.id.Comment)).setText(rollcall.comment);
            if (rollcall.absent)
                ((TextView)convertView.findViewById(R.id.Comment)).setTextColor(ContextCompat.getColor(getContext(), R.color.textSecond));
            else
                ((TextView)convertView.findViewById(R.id.Comment)).setTextColor(ContextCompat.getColor(getContext(), R.color.text));

            return convertView;
        }
    }
}
