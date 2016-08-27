package org.zankio.cculife.ui.ecourse;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;
import org.zankio.ccudata.ecourse.model.File;
import org.zankio.ccudata.ecourse.model.Score;
import org.zankio.ccudata.ecourse.model.ScoreGroup;
import org.zankio.cculife.R;
import org.zankio.cculife.services.DownloadService;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCourseData;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Subscriber;


public class CourseScoreFragment extends BaseMessageFragment
        implements ExpandableListView.OnChildClickListener,  IGetLoading {
    private Course course;
    private ScoreAdapter adapter;
    private ExpandableListView list;
    private boolean loaded;
    private IGetCourseData context;
    private CourseFragment.LoadingListener loadedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context = (IGetCourseData) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IGetCourseData");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_course_score, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new ScoreAdapter();
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
        course = context.getCourse(id);
        if (course == null) {
            getFragmentManager().popBackStack("list", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        course.getScore()
                .subscribe(new Subscriber<Response<ScoreGroup[], CourseData>>() {
                    private boolean noData = true;
                    @Override
                    public void onCompleted() {
                        if (noData)
                            message().show("沒有成績");

                        setLoaded(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e = ExceptionUtils.extraceException(e);

                        setLoaded(true);
                        message().show(e.getMessage());
                    }

                    @Override
                    public void onNext(Response<ScoreGroup[], CourseData> courseDataResponse) {
                        ScoreGroup[] scoreGroups = courseDataResponse.data();
                        if (scoreGroups == null || scoreGroups.length == 0) {
                            return;
                        }

                        adapter.setScores(scoreGroups);

                        for (int i = 0; i < adapter.getGroupCount(); i++) {
                            list.expandGroup(i);
                        }

                        noData = false;
                        message().hide();

                    }

                    @Override
                    public void onStart() {
                        super.onStart();
                        setLoaded(false);
                        message().show("讀取中...", true);
                    }
                });
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

    @Override
    public boolean isLoading() {
        return !this.loaded;
    }

    @Override
    public void setLoadedListener(CourseFragment.LoadingListener listener) {
        this.loadedListener = listener;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
        if(loadedListener != null) loadedListener.call(loaded);
    }

    public class ScoreAdapter extends BaseExpandableListAdapter {
        private ScoreGroup[] scores;

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
            LayoutInflater inflater = LayoutInflater.from(getContext());
            ScoreGroup score = (ScoreGroup) getGroup(groupPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_score, parent, false);
            }

            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.ScoreCate));
            ((TextView)convertView.findViewById(R.id.Name)).setText(score.name);
            ((TextView)convertView.findViewById(R.id.Score)).setText(score.score);
            ((TextView)convertView.findViewById(R.id.Rank)).setText(score.rank);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            Score score = (Score) getChild(groupPosition, childPosition);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_score, parent, false);
            }

            ((TextView)convertView.findViewById(R.id.Name)).setText(score.name);
            ((TextView)convertView.findViewById(R.id.Percent)).setText(score.percent);
            ((TextView)convertView.findViewById(R.id.Score)).setText(score.score);
            ((TextView)convertView.findViewById(R.id.Rank)).setText(score.rank);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
