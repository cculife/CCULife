package org.zankio.cculife.ui.ScoreQuery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.R;
import org.zankio.cculife.CCUService.ScoreQuery;
import org.zankio.cculife.ui.Base.BasePage;
import org.zankio.cculife.ui.Base.onDataLoadListener;

public class GradePage extends BasePage implements onDataLoadListener<ScoreQuery.Grade> {

    private ScoreQuery.Grade grade;
    private ListView list;
    private ScoreAdapter adapter;

    public GradePage(LayoutInflater inflater, ScoreQuery.Grade grade) {
        super(inflater);
        this.grade = grade;
    }

    @Override
    protected View createView() {
        return inflater.inflate(R.layout.fragment_grade, null);
    }

    @Override
    public void initViews() {
        adapter = new ScoreAdapter(grade.Scores);

        list = (ListView) PageView.findViewById(R.id.list);
        list.setAdapter(adapter);


        ((TextView)PageView.findViewById(R.id.description)).setText(grade.Description.replace("本學期共", ""));

    }

    @Override
    public void onDataLoaded(ScoreQuery.Grade data) {
        adapter.setScores(data.Scores);
    }


    public class ScoreAdapter extends BaseAdapter {

        private ScoreQuery.Score[] scores;

        public ScoreAdapter(ScoreQuery.Score[] scores) {
            this.scores = scores;
        }

        public void setScores(ScoreQuery.Score[] scores){
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
            ScoreQuery.Score score = (ScoreQuery.Score) getItem(position);

            View view = inflater.inflate(R.layout.item_grade_score, null);
            ((TextView)view.findViewById(R.id.CourseID)).setText(score.CoruseID);
            ((TextView)view.findViewById(R.id.Name)).setText(score.CourseName);
            ((TextView)view.findViewById(R.id.Credit)).setText(score.Credit);
            ((TextView)view.findViewById(R.id.Score)).setText(score.Score);
            return view;
        }
    }

}
