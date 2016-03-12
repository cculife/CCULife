package org.zankio.cculife.ui.ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.CCUService.ecourse.source.remote.HomeworkContentSource;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;


public class CourseHomeworkFragment extends BaseMessageFragment
        implements IOnUpdateListener<Homework[]>, AdapterView.OnItemClickListener {
    private Course course;
    private Ecourse ecourse;
    private HomeworkAdapter adapter;
    private boolean loading;
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

        loading = course.getHomework(this);

        if (loading)
            showMessage("讀取中...", true);
    }

    @Override
    public void onNext(String type, Homework[] data, BaseSource source) {
        this.loading = false;
        onHomewrokUpdate(data);
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        this.loading = false;
        showMessage(err.getMessage());
    }

    @Override
    public void onComplete(String type) { }

    private IOnUpdateListener<Homework> homeworkContentListener = new OnUpdateListener<Homework>() {
        @Override
        public void onNext(String type, Homework data, BaseSource source) {
            onHomewrokContentUpdate(null, data);
        }

        @Override
        public void onError(String type, Exception err, BaseSource source) {
            onHomewrokContentUpdate(err.getMessage(), null);
        }
    };

    private void onHomewrokContentUpdate(String err, Homework homework) {
        Context context = getContext();
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

        if (url != null) {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(homework.contentUrl)));
        } else if (content != null) {
            message.setText(Html.fromHtml(content));
            message.setAutoLinkMask(Linkify.WEB_URLS);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            message.setPadding(20, 20, 20, 20);
            builder.setView(message);

            //builder.setMessage(Html.fromHtml(announce.getContent()));
            if (homework != null) builder.setTitle(homework.title);
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();
            if (context instanceof Activity && ((Activity)context).isFinishing()) return;
            dialog.show();
        } else {
            Toast.makeText(context, "讀取題目錯誤", Toast.LENGTH_SHORT).show();
        }

    }

    private void onHomewrokUpdate(Homework[] homework) {
        if(homework == null || homework.length == 0) {
            showMessage("沒有作業");
            return;
        }

        adapter.setHomeworks(homework);
        hideMessage();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Homework homework = (Homework) parent.getAdapter().getItem(position);
        homework.getContent(homeworkContentListener);
    }
    }

    public class HomeworkAdapter extends BaseAdapter {

        private Homework[] homeworks;
        private LayoutInflater inflater;

        public HomeworkAdapter() {
            this.inflater = LayoutInflater.from(getContext());
        }

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
            Homework homework = (Homework) getItem(position);

            View view = convertView == null ? inflater.inflate(R.layout.item_homework, null) : convertView;
            ((TextView)view.findViewById(R.id.Title)).setText(homework.title);
            ((TextView)view.findViewById(R.id.Deadline)).setText(homework.deadline);
            ((TextView)view.findViewById(R.id.Score)).setText(homework.score);

            return view;
        }
    }
}
