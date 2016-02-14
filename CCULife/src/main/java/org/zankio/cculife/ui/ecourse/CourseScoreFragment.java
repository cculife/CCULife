package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.File;
import org.zankio.cculife.CCUService.ecourse.model.Score;
import org.zankio.cculife.CCUService.ecourse.model.ScoreGroup;
import org.zankio.cculife.R;
import org.zankio.cculife.services.DownloadService;
import org.zankio.cculife.ui.base.IGetCourseData;


public class CourseScoreFragment extends BaseMessageFragment
        implements ExpandableListView.OnChildClickListener, IOnUpdateListener<ScoreGroup[]> {
    private Course course;
    private ScoreAdapter adapter;
    private ExpandableListView list;
    private IGetCourseData courseDataContext;
    private boolean loading;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            courseDataContext = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetCourseData");
        }
        String id = getArguments().getString("id");
        this.course = courseDataContext.getCourse(id);

        adapter = new ScoreAdapter(context);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_score, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        list = (ExpandableListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setGroupIndicator(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        courseChange(getArguments().getString("id"));
    }

    public void courseChange(String id) {
        course = courseDataContext.getCourse(id);
        loading = course.getScore(this);

        if (loading)
            showMessage("讀取中...", true);
    }

    @Override
    public void onNext(String type, ScoreGroup[] scoreGroups, BaseSource source) {
        this.loading = false;
        if (scoreGroups == null || scoreGroups.length == 0) {
            showMessage("沒有成績");
            return;
        }

        adapter.setScores(scoreGroups);

        for (int i = 0; i < adapter.getGroupCount(); i++) {
            list.expandGroup(i);
        }

        hideMessage();
    }

    @Override
    public void onComplete(String type) {
    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        this.loading = false;
        showMessage(err.getMessage());
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        File file;
        String filename;
        file = (File) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
        assert file != null;
        filename = file.name != null ? file.name : URLUtil.guessFileName(file.url, null, null);
        DownloadService.downloadFile(getContext(), file.url, filename);

        Toast.makeText(getContext(), "下載 : " + filename, Toast.LENGTH_SHORT).show();
        return false;
    }

    public class ScoreAdapter extends BaseExpandableListAdapter {
        private LayoutInflater inflater;
        private ScoreGroup[] scores;

        public ScoreAdapter(Context context) {
            this.inflater = LayoutInflater.from(context);
        }

        public void setScores(ScoreGroup[] scores){
            this.scores = scores;
            this.notifyDataSetChanged();
        }


        @Override
        public int getGroupCount() {
            return scores == null ? 0 : scores.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return scores[groupPosition].scores == null ? 0 : scores[groupPosition].scores.length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return scores == null ? null :scores[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return scores == null || scores[groupPosition].scores == null ? null :scores[groupPosition].scores[childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ScoreGroup score = (ScoreGroup) getGroup(groupPosition);
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.item_score, null);
            } else {
                view = convertView;
            }
            view.setBackgroundColor(inflater.getContext().getResources().getColor(R.color.ScoreCate));
            ((TextView)view.findViewById(R.id.Name)).setText(score.name);
            ((TextView)view.findViewById(R.id.Score)).setText(score.score);
            ((TextView)view.findViewById(R.id.Rank)).setText(score.rank);
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Score score = (Score) getChild(groupPosition, childPosition);

            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.item_score, null);
            } else {
                view = convertView;
            }
            ((TextView)view.findViewById(R.id.Name)).setText(score.name);
            ((TextView)view.findViewById(R.id.Percent)).setText(score.percent);
            ((TextView)view.findViewById(R.id.Score)).setText(score.score);
            ((TextView)view.findViewById(R.id.Rank)).setText(score.rank);
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
