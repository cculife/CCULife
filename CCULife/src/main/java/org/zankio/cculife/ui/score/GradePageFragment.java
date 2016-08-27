package org.zankio.cculife.ui.score;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.ccudata.sourcequery.model.Grade;
import org.zankio.ccudata.sourcequery.model.Score;
import org.zankio.cculife.R;

import rx.Subscriber;

public class GradePageFragment extends Fragment {

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
    public void onResume() {
        super.onResume();

        int index = getArguments().getInt(ARG_GRADE);

        gradeDataContext.getGrade(index).subscribe(new Subscriber<Grade>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(Grade grade) {
                adapter.setScores(grade.scores);
                ((TextView)getView().findViewById(R.id.description)).setText(grade.description.replace("本學期共", ""));

            }
        });
    }

    public class ScoreAdapter extends BaseAdapter {
        private Score[] scores;

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
            LayoutInflater inflater = LayoutInflater.from(getContext());
            Score score = (Score) getItem(position);

            if (convertView == null)
                    convertView = inflater.inflate(R.layout.item_grade_score, parent, false);

            ((TextView)convertView.findViewById(R.id.CourseID)).setText(score.coruseID);
            ((TextView)convertView.findViewById(R.id.Name)).setText(score.courseName);
            ((TextView)convertView.findViewById(R.id.Credit)).setText(score.credit);
            ((TextView)convertView.findViewById(R.id.Score)).setText(score.score);
            return convertView;
        }
    }
}
