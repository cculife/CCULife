package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.model.Classmate;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;

import rx.Subscriber;

public class CourseClassmateFragment extends BaseMessageFragment {

    private GridView list;
    private ClassmateAdapter adapter;
    private IGetCourseData context;

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ClassmateAdapter();
        list = (GridView)view.findViewById(R.id.list);
        list.setAdapter(adapter);

        message().show("讀取中...", true);
        String id = getArguments().getString("id");
        Course course = context.getCourse(id);
        if (course != null) course.getClassmate().subscribe(new Subscriber<Response<Classmate[], CourseData>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                message().show("沒有資料");
            }

            @Override
            public void onNext(Response<Classmate[], CourseData> courseDataResponse) {
                Classmate[] classmates = courseDataResponse.data();
                if (classmates == null || classmates.length == 0) {
                    message().show("沒有資料");
                    return;
                }

                adapter.setClassmate(classmates);
                message().hide();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((BaseFragmentActivity) getActivity()).getSupportActionBar().setTitle("Classmate");
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_classmate, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.course_classmate, menu);
    }

    public class ClassmateAdapter extends BaseAdapter {

        Classmate[] classmates;

        public void setClassmate(Classmate[] classmates) {
            this.classmates = classmates;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return classmates == null ? 0 : classmates.length;
        }

        @Override
        public Object getItem(int position) {
            return classmates == null ? null : classmates[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            Classmate classmate = (Classmate)getItem(position);
            if (convertView == null) convertView = inflater.inflate(R.layout.item_classmate, parent, false);

            //((TextView)convertView.findViewById(R.id.StudentId)).setText(classmate.studentId);
            ((TextView)convertView.findViewById(R.id.Name)).setText(classmate.name);
            ((TextView)convertView.findViewById(R.id.Department)).setText(classmate.department);
            //((TextView)convertView.findViewById(R.id.Gender)).setText(classmate.gender);

            return convertView;
        }
    }

}
