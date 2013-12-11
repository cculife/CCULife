package org.zankio.cculife.ui.Ecourse;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BasePage;

public class CourseScorePage extends BasePage {

    private Ecourse.Course course;
    private ExpandableListView list;
    private ScoreAdapter adapter;

    public CourseScorePage(LayoutInflater inflater, Ecourse.Course course) {
        super(inflater);
        this.course = course;

    }

    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_course_score, null);
    }

    @Override
    public View getMainView() {
        return PageView.findViewById(R.id.list);
    }

    @Override
    public void initViews() {
        adapter = new ScoreAdapter();

        list = (ExpandableListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setGroupIndicator(null);
        new LoadScoreDataAsyncTask(adapter).execute();
    }

    public class LoadScoreDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, Ecourse.Scores[]> {
        private ScoreAdapter adapter;

        public LoadScoreDataAsyncTask(ScoreAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        protected Ecourse.Scores[] _doInBackground(Void... params) throws Exception {
            if(course == null) throw new Exception("請重試...");
            return course.getScore();
        }

        @Override
        protected void onError(String msg) {
            showMessage(msg);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showMessage("讀取中...", true);
        }

        @Override
        protected void _onPostExecute(Ecourse.Scores[] result){
            if (result == null || result.length == 0) {
                showMessage("沒有成績");
                return;
            }

            adapter.setScores(result);

            for (int i = 0; i < adapter.getGroupCount(); i++) {
                list.expandGroup(i);
            }
            hideMessage();
        }
    }

    public class ScoreAdapter extends BaseExpandableListAdapter {

        private Ecourse.Scores[] scores;

        public void setScores(Ecourse.Scores[] scores){
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
            Ecourse.Scores score = (Ecourse.Scores) getGroup(groupPosition);
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.item_score, null);
            } else {
                view = convertView;
            }
            view.setBackgroundColor(inflater.getContext().getResources().getColor(R.color.ScoreCate));
            ((TextView)view.findViewById(R.id.Name)).setText(score.Name);
            ((TextView)view.findViewById(R.id.Score)).setText(score.Score);
            ((TextView)view.findViewById(R.id.Rank)).setText(score.Rank);
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            Ecourse.Score score = (Ecourse.Score) getChild(groupPosition, childPosition);

            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.item_score, null);
            } else {
                view = convertView;
            }
            ((TextView)view.findViewById(R.id.Name)).setText(score.Name);
            ((TextView)view.findViewById(R.id.Percent)).setText(score.Percent);
            ((TextView)view.findViewById(R.id.Score)).setText(score.Score);
            ((TextView)view.findViewById(R.id.Rank)).setText(score.Rank);
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    public class ScoreAdapter1 extends BaseAdapter {

        private Ecourse.Scores[] scores;

        public void setScores(Ecourse.Scores[] scores){
            this.scores = scores;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return scores == null ? 0 : scores.length;
        }

        @Override
        public Object getItem(int position) {
            return scores == null ? null : scores[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Ecourse.Score score = (Ecourse.Score) getItem(position);
            View view;

            if (convertView == null) {
                view = inflater.inflate(R.layout.item_score, null);
            } else {
                view = convertView;
            }
            ((TextView)view.findViewById(R.id.Name)).setText(score.Name);
            ((TextView)view.findViewById(R.id.Percent)).setText(score.Percent);
            ((TextView)view.findViewById(R.id.Score)).setText(score.Score);
            ((TextView)view.findViewById(R.id.Rank)).setText(score.Rank);
            return view;
        }
    }

}
