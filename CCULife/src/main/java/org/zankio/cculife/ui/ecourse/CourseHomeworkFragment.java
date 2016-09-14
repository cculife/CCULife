package org.zankio.cculife.ui.ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.Homework;
import org.zankio.ccudata.ecourse.model.HomeworkData;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Subscriber;


public class CourseHomeworkFragment extends BaseMessageFragment
        implements AdapterView.OnItemClickListener, IGetLoading {
    private Course course;
    private HomeworkAdapter adapter;
    private IGetCourseData context;
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
        return inflater.inflate(R.layout.fragment_course_homework, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new HomeworkAdapter();
        ListView list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

        courseChange(getArguments().getString("id"));
    }

    public void courseChange(String id) {
        course = context.getCourse(id);

        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        course.getHomework()
                .subscribe(new Subscriber<Response<Homework[], CourseData>>() {
                    private boolean noData = true;
                    @Override
                    public void onCompleted() {
                        if (noData)
                            message().show("沒有作業");

                        setLoaded(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e = ExceptionUtils.extraceException(e);

                        setLoaded(true);
                        message().show(e.getMessage());
                    }

                    @Override
                    public void onNext(Response<Homework[], CourseData> courseDataResponse) {
                        Homework[] homework = courseDataResponse.data();

                        if(homework == null || homework.length == 0) {
                            return;
                        }

                        adapter.setHomeworks(homework);
                        noData = false;
                        message().hide();
                    }

                    @Override
                    public void onStart() {
                        setLoaded(false);
                        message().show("讀取中...", true);
                    }
                });
    }

    private void onHomeworkContentUpdate(String err, Homework homework) {
        Context context = getContext();

        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView message = new TextView(context);
        String content = null, url = null;
        if (err != null)
            content = err;
        else {
            switch (homework.getContentType()) {
                case 1:
                    url = homework.contentUrl;
                    break;
                case 0:
                    content = homework.content;
                    break;
                default:
                    content = "讀取錯誤";
            }
        }

        // homework is a url
        if (url != null) {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(homework.contentUrl)));
            return;
        }

        // homework is text
        if (content != null) {
            message.setText(Html.fromHtml(content));
            message.setAutoLinkMask(Linkify.WEB_URLS);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            message.setPadding(20, 20, 20, 20);
            builder.setView(message);
            if (homework != null) builder.setTitle(homework.title);

            builder.setPositiveButton("確定", (dialog, which) -> {
                dialog.dismiss();
            });

            final AlertDialog dialog = builder.create();

            // check activity no isFinishing
            if (context instanceof Activity && ((Activity)context).isFinishing()) return;

            dialog.show();
        } else {
            Toast.makeText(context, "讀取題目錯誤", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Homework homework = (Homework) parent.getAdapter().getItem(position);
        homework.getContent().subscribe(new Subscriber<Response<Homework, HomeworkData>>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                onHomeworkContentUpdate(e.getMessage(), null);
            }

            @Override
            public void onNext(Response<Homework, HomeworkData> response) {
                onHomeworkContentUpdate(null, response.data());
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

    public class HomeworkAdapter extends BaseAdapter {
        private Homework[] homeworks;

        public void setHomeworks(Homework[] homework){
            this.homeworks = homework;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return homeworks == null ? 0 : homeworks.length;
        }

        @Override
        public Object getItem(int position) {
            return homeworks == null ? null : homeworks[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            Homework homework = (Homework) getItem(position);

            if (convertView == null)
                convertView = inflater.inflate(R.layout.item_homework, parent, false);

            ((TextView)convertView.findViewById(R.id.Title)).setText(homework.title);
            ((TextView)convertView.findViewById(R.id.Deadline)).setText(homework.deadline);
            ((TextView)convertView.findViewById(R.id.Score)).setText(homework.score);

            return convertView;
        }
    }
}
