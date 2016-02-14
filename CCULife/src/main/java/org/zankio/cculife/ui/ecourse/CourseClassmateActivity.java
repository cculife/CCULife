package org.zankio.cculife.ui.ecourse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.model.Classmate;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseActivity;

public class CourseClassmateActivity extends BaseActivity implements IOnUpdateListener<Classmate[]> {

    public static Course course;
    private ListView list;
    private ClassmateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courseclassmate);

        adapter = new ClassmateAdapter();
        list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        setMessageView(R.id.list);

        showMessage("讀取中...", true);
        if (course != null) course.getClassmate(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_classmate, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        course = null;
    }

    @Override
    public void onNext(String type, Classmate[] classmates, BaseSource source) {
        if (classmates == null || classmates.length == 0) {
            showMessage("沒有資料");
            return;
        }

        adapter.setClassmate(classmates);
        hideMessage();
    }

    @Override
    public void onComplete(String type) { }
    @Override
    public void onError(String type, Exception err, BaseSource source) { showMessage("沒有資料"); }

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
            LayoutInflater inflater = LayoutInflater.from(CourseClassmateActivity.this);

            Classmate classmate = (Classmate)getItem(position);
            if (convertView == null) convertView = inflater.inflate(R.layout.item_classmate, parent, false);

            ((TextView)convertView.findViewById(R.id.StudentId)).setText(classmate.studentId);
            ((TextView)convertView.findViewById(R.id.Name)).setText(classmate.name);
            ((TextView)convertView.findViewById(R.id.Department)).setText(classmate.department);
            ((TextView)convertView.findViewById(R.id.Gender)).setText(classmate.gender);

            return convertView;
        }
    }

}
