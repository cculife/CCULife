package org.zankio.cculife.ui.ecourse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.model.Announce;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceContentSource;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;

public class CourseAnnounceFragment
        extends BaseMessageFragment
        implements AdapterView.OnItemClickListener, IOnUpdateListener<Announce[]> {
    private Course course;
    private AnnounceAdapter adapter;
    private boolean loading;
    private IGetCourseData courseDataContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("course", "onAttach");

        try {
            courseDataContext = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetCourseData");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("course", "onCreate");
        adapter = new AnnounceAdapter(getActivity());
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
        course = courseDataContext.getCourse(id);
        loading = course.getAnnounces(this);

        if (loading)
            showMessage("讀取中...", true);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Announce announce = (Announce) adapterView.getAdapter().getItem(i);
        announce.getContent(announceContentListener);
    }


    private IOnUpdateListener<Announce> announceContentListener = new OnUpdateListener<Announce>() {
        @Override
        public void onNext(String type, Announce announce, BaseSource source) {
            if (AnnounceContentSource.TYPE.equals(type))
                onAnnounceContentUpdate(announce);
        }
    };

    @Override
    public void onNext(String type, Announce[] data, BaseSource source) {
        this.loading = false;
        onAnnounceUpdate(data);
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        this.loading = false;
        showMessage(err.getMessage());
    }

    @Override
    public void onComplete(String type) { }

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
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        if (context instanceof Activity && ((Activity)context).isFinishing()) return;
        dialog.show();
    }

    private void onAnnounceUpdate(Announce[] announces) {
        if(announces == null || announces.length == 0) {
            showMessage("沒有公告");
            return;
        }
        adapter.setAnnounces(announces);
        hideMessage();
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
