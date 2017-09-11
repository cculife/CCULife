package org.zankio.cculife.ui.score;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import org.apache.commons.lang.ArrayUtils;
import org.zankio.ccudata.base.model.AuthData;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.portal.model.ScoreQueryPortalData;
import org.zankio.ccudata.sourcequery.ScoreQuery;
import org.zankio.ccudata.sourcequery.model.Grade;
import org.zankio.ccudata.sourcequery.source.remote.GradesInquiriesSource;
import org.zankio.cculife.R;
import org.zankio.cculife.UserManager;
import org.zankio.cculife.ui.base.BaseFragmentActivity;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.ReplaySubject;

public class ScoreQueryActivity extends BaseFragmentActivity
        implements IGetGradeData {

    private GradePageAdapter mGradePageAdapter;
    private ViewPager mViewPager;
    private ScoreQuery scoreQuery;
    private Grade[] grades;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorequery);

        //initial message view
        message().content(R.id.pager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGradePageAdapter = new GradePageAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mGradePageAdapter);

        setSSOService(new ScoreQueryPortalData());

        message().show("讀取中...", true);
        getGrade().subscribe(new Subscriber<Grade[]>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) {
                e = ExceptionUtils.extraceException(e);

                message().show(e.getMessage());
            }

            @Override
            public void onNext(Grade[] grades) {
                ArrayUtils.reverse(grades);
                ScoreQueryActivity.this.grades = grades;

                message().hide();

                if(grades == null || grades.length == 0) {
                    message().show("沒有成績");
                    return;
                }

                mGradePageAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.score, menu);
        return true;
    }

    @Override
    public Observable<Grade> getGrade(int i) {
        ReplaySubject<Grade> subject = ReplaySubject.createWithSize(1);
        getGrade().subscribe(new Subscriber<Grade[]>() {
            @Override
            public void onCompleted() {
               subject.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subject.onError(e);
            }

            @Override
            public void onNext(Grade[] grades) {
                subject.onNext(grades[i]);
            }
        });

        return subject;
    }

    public Observable<Grade[]> getGrade() {
        ReplaySubject<Grade[]> subject =
                ScoreDataFragment
                        .getFragment(getSupportFragmentManager())
                        .getGrades();

        if (subject != null)
            return subject;

        subject = ReplaySubject.createWithSize(1);

        if (scoreQuery == null)
            scoreQuery = new ScoreQuery(this);

        Observable<Response<Grade[], AuthData>> observable;
        UserManager userManager = UserManager.getInstance(this);
        observable = scoreQuery.fetch(GradesInquiriesSource.request(
                userManager.getUsername(),
                userManager.getPassword()
        ));

        final ReplaySubject<Grade[]> finalSubject = subject;
        observable.subscribe(new Subscriber<Response<Grade[], AuthData>>() {
            @Override
            public void onCompleted() { finalSubject.onCompleted(); }

            @Override
            public void onError(Throwable e) {
                finalSubject.onError(e);
            }

            @Override
            public void onNext(Response<Grade[], AuthData> response) {
                finalSubject.onNext(response.data());
            }
        });

        ScoreDataFragment.getFragment(getSupportFragmentManager()).setGrades(subject);
        return subject;
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
