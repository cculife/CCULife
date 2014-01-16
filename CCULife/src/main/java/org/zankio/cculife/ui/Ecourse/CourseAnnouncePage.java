package org.zankio.cculife.ui.Ecourse;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BasePage;

public class CourseAnnouncePage extends BasePage implements AdapterView.OnItemClickListener {

    private Ecourse.Course course;

    private static Ecourse.Course _course;
    private static Ecourse.Announce[] _announces;
    private static LoadAnnounceDataAsyncTask _announceTask;

    public CourseAnnouncePage(LayoutInflater inflater, Ecourse.Course course) {
        super(inflater);
        this.course = course;

        if(_course != course) {
            if(_announceTask != null) _announceTask.cancel(false);

            _announces = null;
            _announceTask = null;
            _course = course;
        }

    }



    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_announce, null);
    }

    @Override
    public View getMainView() {
        return PageView.findViewById(R.id.list);
    }

    @Override
    public void initViews() {
        AnnounceAdapter adapter = new AnnounceAdapter();

        ListView list = (ListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        new LoadAnnounceDataAsyncTask(adapter).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Ecourse.Announce announce = (Ecourse.Announce) parent.getAdapter().getItem(position);
        new LoadAnnounceContentAnsyncTask().execute(announce);
    }

    public class LoadAnnounceContentAnsyncTask extends AsyncTask<Ecourse.Announce, Void, Ecourse.Announce>{

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Ecourse.Announce doInBackground(Ecourse.Announce... params) {
            params[0].getContent();
            return params[0];
        }

        @Override
        protected void onPostExecute(Ecourse.Announce announce) {
            AlertDialog.Builder builder = new AlertDialog.Builder(inflater.getContext());

            builder.setMessage(Html.fromHtml(announce.getContent()));
            builder.setTitle(announce.Title);
            builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public class LoadAnnounceDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Ecourse.Announce[]> {
        private AnnounceAdapter adapter;
        public LoadAnnounceDataAsyncTask(AnnounceAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(_announces == null)
                showMessage("讀取中...", true);
        }

        @Override
        protected Ecourse.Announce[] _doInBackground(Void... params) throws Exception {
            return _announces == null ?
                    _announces = course.getAnnounces() :
                    _announces;
        }

        @Override
        protected void _onPostExecute(Ecourse.Announce[] result){
            if(result == null || result.length == 0) {
                showMessage("沒有公告");
                return;
            }

            adapter.setAnnounces(result);
            hideMessage();
        }
    }




    public class AnnounceAdapter extends BaseAdapter {

        private Ecourse.Announce[] announces;

        public void setAnnounces(Ecourse.Announce[] announces){
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
            Ecourse.Announce announce = (Ecourse.Announce) getItem(position);

            View view = inflater.inflate(R.layout.item_announce, null);
            ((TextView)view.findViewById(R.id.Title)).setText(announce.Title);
            ((TextView)view.findViewById(R.id.Date)).setText(announce.Date);
            if (announce.isnew) view.setBackgroundColor(inflater.getContext().getResources().getColor(R.color.Unread));
            else view.setBackgroundColor(0);

            return view;
        }
    }

}
