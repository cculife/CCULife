package org.zankio.cculife.ui.Ecourse;


import android.app.Activity;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BasePage;

public class CourseFilePage extends BasePage {

    private Ecourse.Course course;
    private ListView list;
    private FileAdapter adapter;

    public CourseFilePage(LayoutInflater inflater, Ecourse.Course course) {
        super(inflater);
        this.course = course;
    }

    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_file, null);
    }

    @Override
    public View getMainView() {
        return PageView.findViewById(R.id.list);
    }

    @Override
    public void initViews() {
        adapter = new FileAdapter();

        list = (ListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Ecourse.File file = (Ecourse.File) parent.getAdapter().getItem(position);
                String filename = file.Name != null ? file.Name : URLUtil.guessFileName(file.URL, null, null);

                DownloadManager manager = (DownloadManager) inflater.getContext().getSystemService(Activity.DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(file.URL));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                else
                    request.setShowRunningNotification(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                manager.enqueue(request);
            }
        });
        new LoadFileDataAsyncTask(adapter).execute();
    }

    public class LoadFileDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Ecourse.File[]> {
        private FileAdapter adapter;

        public LoadFileDataAsyncTask(FileAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessage("讀取中...", true);
        }

        @Override
        protected Ecourse.File[] _doInBackground(Void... params) throws Exception {
            if(course == null) throw new Exception("請重試...");
            return course.getFiles();
        }

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void _onPostExecute(Ecourse.File[] result){
            if (result == null || result.length == 0) {
                showMessage("沒有檔案");
                return;
            }

            adapter.setFiles(result);
            hideMessage();
        }
    }

    public class FileAdapter extends BaseAdapter {

        private Ecourse.File[] files;

        public void setFiles(Ecourse.File[] files){
            this.files = files;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return files == null ? 0 : files.length;
        }

        @Override
        public Object getItem(int position) {
            return files == null ? null : files[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Ecourse.File file = (Ecourse.File) getItem(position);
            View view;

            if(convertView == null)
                view = inflater.inflate(R.layout.item_file, null);
            else
                view = convertView;

            ((TextView)view.findViewById(R.id.Name)).setText(file.Name);
            ((TextView)view.findViewById(R.id.Size)).setText(file.Size != null ? file.Size : "");

            return view;
        }
    }

}
