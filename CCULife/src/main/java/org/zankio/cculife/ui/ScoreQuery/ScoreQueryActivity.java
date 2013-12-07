package org.zankio.cculife.ui.ScoreQuery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;

import org.zankio.cculife.CCUService.ScoreQuery;
import org.zankio.cculife.R;
import org.zankio.cculife.override.AsyncTaskWithErrorHanding;
import org.zankio.cculife.ui.Base.BaseFragmentActivity;

public class ScoreQueryActivity extends BaseFragmentActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;
    private ScoreQuery scoreQuery;
    private ScoreQuery.Grade[] grades;
    private GradePage[] gradePages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorequery);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        setMessageView(R.id.pager);
        setSSOService(new org.zankio.cculife.CCUService.PortalService.ScoreQuery());
        new LoadGradeDataAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.score, menu);
        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new DummySectionFragment(grades, gradePages);
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return grades == null ? 0 : grades.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return grades[position].Grade;
        }
    }

    public static class DummySectionFragment extends Fragment {

        public static final String ARG_POSITION = "position";
        private static ScoreQuery.Grade[] grades;
        private static GradePage[] gradePages;

        public DummySectionFragment() {
        }

        public DummySectionFragment(ScoreQuery.Grade[] grades, GradePage[] gradePages) {
            this.grades = grades;
            this.gradePages = gradePages;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int index = getArguments().getInt(ARG_POSITION);
            GradePage gradePage = new GradePage(inflater, grades[index]);
            gradePages[index] = gradePage;
            return gradePage.getView();
        }
    }

    public class LoadGradeDataAsyncTask extends AsyncTaskWithErrorHanding<Void, Void, ScoreQuery.Grade[]> {

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
        protected ScoreQuery.Grade[] _doInBackground(Void... params) throws Exception {
            if(scoreQuery == null) scoreQuery = new ScoreQuery(ScoreQueryActivity.this);
            return scoreQuery.getGrades();
        }

        @Override
        protected void _onPostExecute(ScoreQuery.Grade[] grades) {
            if(grades == null || grades.length == 0) {
                showMessage("沒有成績");
                return;
            }

            ScoreQueryActivity.this.grades = grades;
            ScoreQueryActivity.this.gradePages = new GradePage[grades.length];
            onDataLoad();
            hideMessage();
        }
    }

    private void onDataLoad() {

        mSectionsPagerAdapter.notifyDataSetChanged();
        for (int i = 0; i < gradePages.length; i++) {
            if(gradePages[i] != null) gradePages[i].onDataLoaded(grades[i]);
        }
    }

}
