package org.zankio.cculife.ui.ScoreQuery;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.sourcequery.model.Grade;
import org.zankio.cculife.CCUService.sourcequery.model.Score;
import org.zankio.cculife.R;

public class GradePageFragment extends Fragment implements IOnUpdateListener<Grade> {

    public static final String ARG_GRADE = "grade";
    private IGetGradeData gradeDataContext;
    private ScoreAdapter adapter;
    private ListView list;

    public GradePageFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            gradeDataContext = (IGetGradeData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetGradeData");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grade, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new ScoreAdapter();

        list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    @Override
    public void onNext(String type, Grade grade, BaseSource source) {
        adapter.setScores(grade.scores);
        ((TextView)getView().findViewById(R.id.description)).setText(grade.description.replace("本學期共", ""));
    }
    @Override
    public void onComplete(String type) { }
    @Override
    public void onError(String type, Exception err, BaseSource source) { }

    @Override
    public void onResume() {
        super.onResume();

        int index = getArguments().getInt(ARG_GRADE);
        gradeDataContext.getGrade(index, this);
    }

    public class ScoreAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private Score[] scores;

        public ScoreAdapter() {
            inflater = LayoutInflater.from(getContext());
        }

        public void setScores(Score[] scores){
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
            Score score = (Score) getItem(position);

            View view = inflater.inflate(R.layout.item_grade_score, null);
            ((TextView)view.findViewById(R.id.CourseID)).setText(score.coruseID);
            ((TextView)view.findViewById(R.id.Name)).setText(score.courseName);
            ((TextView)view.findViewById(R.id.Credit)).setText(score.credit);
            ((TextView)view.findViewById(R.id.Score)).setText(score.score);
            return view;
        }
    }
}
