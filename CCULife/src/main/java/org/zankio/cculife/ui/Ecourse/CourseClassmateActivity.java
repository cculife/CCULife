package org.zankio.cculife.ui.Ecourse;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;

import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BaseActivity;

public class CourseClassmateActivity extends BaseActivity {

    private Ecourse.Course course;
    private ListView list;
    private ClassmateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courseclassmate);
        course = CourseListActivity.ecourse.nowCourse;

        adapter = new ClassmateAdapter();
        list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        setMessageView(R.id.list);

        new LoadClassmateAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.course_classmate, menu);
        return true;
    }

    public class LoadClassmateAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Ecourse.Classmate[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessage("讀取中...", true);
        }

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected Ecourse.Classmate[] _doInBackground(Void... params) throws Exception {
            if(course == null) throw new Exception("請重試...");
            return course.getClassmate();
        }

        @Override
        protected void _onPostExecute(Ecourse.Classmate[] classmates) {
            if (classmates == null || classmates.length == 0) {
                showMessage("沒有資料");
                return;
            }

            adapter.setClassmate(classmates);
            hideMessage();
        }
    }

    public class ClassmateAdapter extends BaseAdapter {

        Ecourse.Classmate[] classmates;

        public void setClassmate(Ecourse.Classmate[] classmates) {
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
            LayoutInflater inflater = (LayoutInflater) CourseClassmateActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Ecourse.Classmate classmate = (Ecourse.Classmate)getItem(position);
            if (convertView == null) convertView = inflater.inflate(R.layout.item_classmate, null);

            ((TextView)convertView.findViewById(R.id.StudentId)).setText(classmate.StudentId);
            ((TextView)convertView.findViewById(R.id.Name)).setText(classmate.Name);
            ((TextView)convertView.findViewById(R.id.Department)).setText(classmate.Department);
            ((TextView)convertView.findViewById(R.id.Gender)).setText(classmate.Gender);

            return convertView;
        }
    }

}
