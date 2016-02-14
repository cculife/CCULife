package org.zankio.cculife.ui.ScoreQuery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.sourcequery.ScoreQueryNew;
import org.zankio.cculife.CCUService.sourcequery.model.Grade;
import org.zankio.cculife.CCUService.sourcequery.source.remote.GradesInquiriesSource;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseFragmentActivity;

import java.util.HashMap;
import java.util.Map;

public class ScoreQueryActivity extends BaseFragmentActivity
        implements IOnUpdateListener<Grade[]>, IGetGradeData{

    GradePageAdapter mGradePageAdapter;

    ViewPager mViewPager;
    private ScoreQueryNew scoreQuery;
    private Grade[] grades;
    private HashMap<Integer, IOnUpdateListener> listeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorequery);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mGradePageAdapter = new GradePageAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mGradePageAdapter);

        setMessageView(R.id.pager);
        setSSOService(new org.zankio.cculife.CCUService.portal.service.ScoreQuery());

        showMessage("讀取中...", true);
        scoreQuery = new ScoreQueryNew(this);
        scoreQuery.fetch(GradesInquiriesSource.TYPE, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.score, menu);
        return true;
    }

    @Override
    public void onComplete(String type) {

    }

    @Override
    public void onError(String type, Exception err, BaseSource source) {
        showMessage(err.getMessage());
    }

    @Override
    public void onNext(String type, Grade[] grades, BaseSource source) {
        this.grades = grades;
        hideMessage();

        if(grades == null || grades.length == 0) {
            showMessage("沒有成績");
            return;
        }

        mGradePageAdapter.notifyDataSetChanged();

        if (listeners != null) {
            for (Map.Entry<Integer, IOnUpdateListener> listenerEntry : listeners.entrySet()) {
                int key = listenerEntry.getKey();
                IOnUpdateListener listener = listeners.remove(key);
                if (listener != null)
                    listener.onNext(null, grades[key], null);
            }
        }

    }

    @Override
    public void getGrade(int i, IOnUpdateListener listener) {
        if (listeners == null) listeners = new HashMap<>();
        listeners.put(i, listener);

        if (this.grades != null) {
            listener = listeners.remove(i);
            if (listener != null) {
                listener.onNext(null, grades[i], null);
            }
        }
    }

    public class GradePageAdapter extends FragmentPagerAdapter {
        public GradePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new GradePageFragment();
            Bundle args = new Bundle();
            args.putInt(GradePageFragment.ARG_GRADE, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return grades == null ? 0 : grades.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return grades[position].grade;
        }
    }

}
