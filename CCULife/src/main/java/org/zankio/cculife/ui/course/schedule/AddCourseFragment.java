package org.zankio.cculife.ui.course.schedule;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.kiki.model.Course;
import org.zankio.ccudata.kiki.model.TimeTable;
import org.zankio.ccudata.kiki.source.local.DatabaseTimeTableSource;
import org.zankio.cculife.KikiCourseAsset;
import org.zankio.cculife.R;


public class AddCourseFragment extends DialogFragment
        implements View.OnClickListener, AdapterView.OnItemClickListener,
        View.OnKeyListener, TextView.OnEditorActionListener {
    private KikiCourseAsset asset;
    private CourseAdapter adapter;
    private EditText keyView;
    private TimeTable timetable;
    private View noResultView;
    private TimeTableDaysFragment.OnTimetableUpdate updateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        asset = new KikiCourseAsset(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        IGetTimeTableData context = (IGetTimeTableData) getActivity();
        // TODO: 2016/9/11 check error
        context.getTimeTable().subscribe(timeTable -> this.timetable = timeTable, Throwable::printStackTrace);

        adapter = new CourseAdapter();

        View view = View.inflate(getContext(),R.layout.add_course, null);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        view.findViewById(R.id.search).setOnClickListener(this);

        noResultView = view.findViewById(R.id.no_result);
        keyView = (EditText) view.findViewById(R.id.key);
        keyView.setOnEditorActionListener(this);
        keyView.setImeActionLabel("搜尋", KeyEvent.KEYCODE_ENTER);

        return new AlertDialog.Builder(getContext())
                .setTitle("增加旁聽課程")
                .setView(view).create();
    }

    @Override
    public void onClick(View v) {
        String key = keyView.getText().toString();
        Course[] course = asset.getFindCourse(key);

        if (course.length > 0)
            noResultView.setVisibility(View.GONE);
        else
            noResultView.setVisibility(View.VISIBLE);

        adapter.setCourse(course);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Course course = (Course) parent.getAdapter().getItem(position);
        // TODO: 2016/9/12 add course and timetable is null
        if (timetable == null || timetable.exist(String.format("%s_%s", course.CourseID, course.ClassID))) {
            this.dismiss();
            return;
        }

        KikiCourseAsset.addCourseToTimeTable(timetable, course);
        timetable.sort();
        new DatabaseTimeTableSource(new Repository(getContext()) {
            @Override
            protected BaseSource[] getSources() {
                return new BaseSource[0];
            }
        }).storeTimeTable(timetable, true);
        updateListener.onUpdate();
        this.dismiss();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            onClick(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            onClick(null);
            return true;
        }
        return false;
    }

    public void setUpdateListener(TimeTableDaysFragment.OnTimetableUpdate updateListener) {
        this.updateListener = updateListener;
    }

    public class CourseAdapter extends BaseAdapter {
        private Course[] courses;
        public void setCourse(Course[] courses) {
            this.courses = courses;
        }

        @Override
        public int getCount() {
            return courses == null ? 0 : courses.length;
        }

        @Override
        public Object getItem(int position) {
            return courses[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (convertView == null) convertView = inflater.inflate(R.layout.item_course_info, parent, false);

            Course course = courses[position];
            ((TextView)convertView.findViewById(R.id.name)).setText(course.Name);
            ((TextView)convertView.findViewById(R.id.course_id)).setText(String.format("%s - %s_%s", course.Dept, course.CourseID, course.ClassID));
            ((TextView)convertView.findViewById(R.id.teacher)).setText(course.Teacher);
            ((TextView)convertView.findViewById(R.id.classroom)).setText(course.ClassRoom);
            ((TextView)convertView.findViewById(R.id.time)).setText(course.Time);
            return convertView;
        }
    }

}
