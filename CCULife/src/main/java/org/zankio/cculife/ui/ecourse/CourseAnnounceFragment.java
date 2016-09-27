package org.zankio.cculife.ui.ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.model.Announce;
import org.zankio.ccudata.ecourse.model.AnnounceData;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Subscriber;

public class CourseAnnounceFragment
        extends BaseMessageFragment
        implements AdapterView.OnItemClickListener, IGetLoading {
    private Course course;
    private AnnounceAdapter adapter;
    private boolean loaded;
    private IGetCourseData context;
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
        return inflater.inflate(R.layout.fragment_course_announce, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new AnnounceAdapter();

        ListView list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        courseChange(getArguments().getString("id"));
    }

    public void courseChange(String id) {
        course = context.getCourse(id);

        // course not in memory
        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        // load announce
        course.getAnnounces()
                .doOnTerminate(() -> setLoaded(true))
                .subscribe(new Subscriber<Response<Announce[], CourseData>>() {
                    private boolean noData = true;
                    @Override
                    public void onStart() {
                        super.onStart();
                        setLoaded(false);
                        message().show("讀取中...", true);
                    }

                    @Override
                    public void onCompleted() {
                        if (noData)
                            message().show("沒有公告");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e = ExceptionUtils.extraceException(e);
                        message().show(e.getMessage());
                    }

                    @Override
                    public void onNext(Response<Announce[], CourseData> courseDataResponse) {
                        Announce[] announces = courseDataResponse.data();
                        if(announces == null || announces.length == 0) {
                            return;
                        }

                        adapter.setAnnounces(announces);
                        noData = false;

                        message().hide();
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Announce announce = (Announce) adapterView.getAdapter().getItem(i);
        if (announce.content != null)
            onAnnounceContentUpdate(announce);
        else
            announce.getContent(true).subscribe(new Subscriber<Response<Announce, AnnounceData>>() {
                @Override
                public void onCompleted() { }

                @Override
                public void onError(Throwable e) {
                    e = ExceptionUtils.extraceException(e);
                    onAnnounceContentUpdate(announce, e.getMessage());
                }

                @Override
                public void onNext(Response<Announce, AnnounceData> response) {
                    onAnnounceContentUpdate(response.data());
                }
            });
    }

    private void onAnnounceContentUpdate(Announce announce) {
        onAnnounceContentUpdate(announce, null);
    }

    private void onAnnounceContentUpdate(Announce announce, String message) {
        Context context = getContext();
        if (context == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView messageView = new TextView(getContext());
        if (message != null) {
            messageView.setText(message);
        } else if (announce.content != null) {
            messageView.setText(Html.fromHtml(announce.content));
        } else {
            messageView.setText("沒有資料");
        }
        messageView.setAutoLinkMask(Linkify.WEB_URLS);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        messageView.setPadding(20, 20, 20, 20);
        builder.setView(messageView);

        builder.setTitle(announce.title);
        builder.setPositiveButton("確定", (dialog, which) -> {
            dialog.dismiss();
        });
        final AlertDialog dialog = builder.create();
        if (context instanceof Activity && ((Activity)context).isFinishing()) return;
        dialog.show();
    }

    @Override
    public boolean isLoading() {
        return !this.loaded;
    }

    @Override
    public void setLoadedListener(CourseFragment.LoadingListener listener) {
        this.loadedListener = listener;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
        if (this.loadedListener != null) loadedListener.call(loaded);
    }

    public class AnnounceAdapter extends BaseAdapter {

        private Announce[] announces;

        public void setAnnounces(Announce[] announces){
            this.announces = announces;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return announces == null ? 0 : announces.length;
        }

        @Override
        public Object getItem(int position) {
            return announces == null ? null : announces[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if (convertView == null) convertView = inflater.inflate(R.layout.item_announce, parent, false);
            Announce announce = (Announce) getItem(position);

            ((TextView)convertView.findViewById(R.id.Title)).setText(announce.title);
            ((TextView)convertView.findViewById(R.id.Date)).setText(announce.date);
            if (announce.isnew) convertView.setBackgroundColor(ContextCompat.getColor(inflater.getContext(), R.color.Unread));
            else convertView.setBackgroundColor(0);

            return convertView;
        }
    }
}
