package org.zankio.cculife.ui.ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
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
    private boolean loading;
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
        Log.e("course", "onCreateView");
        return inflater.inflate(R.layout.fragment_course_announce, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.e("course", "onViewCreated");
        adapter = new AnnounceAdapter(getActivity());
        ListView list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("course", "onResume");
        courseChange(getArguments().getString("id"));
    }

    public void courseChange(String id) {
        course = context.getCourse(id);
        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        course.getAnnounces().subscribe(new Subscriber<Response<Announce[], CourseData>>() {
            @Override
            public void onCompleted() {
                setLoaded(true);
            }

            @Override
            public void onError(Throwable e) {
                e = ExceptionUtils.extraceException(e);

                CourseAnnounceFragment.this.loading = false;
                setLoaded(true);

                message().show(e.getMessage());
            }

            @Override
            public void onNext(Response<Announce[], CourseData> courseDataResponse) {
                CourseAnnounceFragment.this.loading = false;
                onAnnounceUpdate(courseDataResponse.data());
            }
        });

        if (loading) {
            loading = true;
            setLoaded(false);
            message().show("讀取中...", true);
        }
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
        Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        TextView message = new TextView(getContext());
        if (announce.content != null) {
            message.setText(Html.fromHtml(announce.content));
        } else {
            message.setText("沒有資料");
        }
        message.setAutoLinkMask(Linkify.WEB_URLS);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        message.setPadding(20, 20, 20, 20);
        builder.setView(message);

        builder.setTitle(announce.title);
        builder.setPositiveButton("確定", (dialog, which) -> {
            dialog.dismiss();
        });
        final AlertDialog dialog = builder.create();
        if (context instanceof Activity && ((Activity)context).isFinishing()) return;
        dialog.show();
    }

    private void onAnnounceUpdate(Announce[] announces) {
        if(announces == null || announces.length == 0) {
            message().show("沒有公告");
            return;
        }
        adapter.setAnnounces(announces);
        message().hide();
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
        private LayoutInflater inflater;

        public AnnounceAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

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
            View view;

            if (convertView == null) view = inflater.inflate(R.layout.item_announce, parent, false);
            else view = convertView;

            LayoutInflater inflater = getActivity().getLayoutInflater();
            Announce announce = (Announce) getItem(position);

            ((TextView)view.findViewById(R.id.Title)).setText(announce.title);
            ((TextView)view.findViewById(R.id.Date)).setText(announce.date);
            if (announce.isnew) view.setBackgroundColor(inflater.getContext().getResources().getColor(R.color.Unread));
            else view.setBackgroundColor(0);

            return view;
        }
    }
}
