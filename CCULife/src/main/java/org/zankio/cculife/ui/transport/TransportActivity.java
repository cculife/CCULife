package org.zankio.cculife.ui.transport;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.bus.Bus;
import org.zankio.cculife.CCUService.bus.model.BusLineRequest;
import org.zankio.cculife.CCUService.bus.model.BusStop;
import org.zankio.cculife.CCUService.bus.source.remote.BusStateSource;
import org.zankio.cculife.CCUService.train.Train;
import org.zankio.cculife.CCUService.train.model.TrainTimetable;
import org.zankio.cculife.CCUService.train.source.remote.TrainStopStatusSource;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseActivity;
import org.zankio.cculife.ui.base.CacheFragment;
import org.zankio.cculife.ui.base.IGetCache;
import org.zankio.cculife.ui.base.helper.FragmentPagerHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static org.zankio.cculife.ui.base.helper.FragmentPagerHelper.Page;

public class TransportActivity extends BaseActivity
        implements IGetBusData, IGetCache, View.OnClickListener, IGetTrainData {

    private static final String TAG_TRANSPORT_CACHE = "TAG_TRANSPORT_CACHE";
    private static final String KEY_TRAIN_PREFIX = "KEY_TRAIN";
    private static final String KEY_BUS_PREFIX = "KEY_BUS";

    private static final int CACHE_TIME = 60 * 1000;
    private FragmentPagerHelper mPagerHelper;
    private CacheFragment mCache;
    private ViewPager mViewPager;
    private Bus bus;
    private Train train;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport);
        initToolbar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bus = new Bus(this);
        train = new Train(this);

        mCache = CacheFragment.get(getSupportFragmentManager(), TAG_TRANSPORT_CACHE);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mPagerHelper = new FragmentPagerHelper(getSupportFragmentManager(), new Page[]{
                new Page(getString(R.string.cybus), BusFragment.getInstance(new BusLineRequest[]{
                        new BusLineRequest("7309", "0", "1", "嘉義 -> 中正"),
                        new BusLineRequest("7309", "0", "2", "中正 -> 嘉義"),
                        new BusLineRequest("7309", "A", "1", "嘉義 -> 中正 -> 南華"),
                        new BusLineRequest("7309", "A", "2", "南華 -> 中正 -> 嘉義"),
                })),
                new Page(getString(R.string.solarbus), BusFragment.getInstance(new BusLineRequest[]{
                        new BusLineRequest("7005", "0", "1", "中正 -> 台北"),
                        new BusLineRequest("7005", "0", "2", "台北 -> 中正"),
                        new BusLineRequest("7005", "A", "1", "中正 -> 中壢, 桃園 -> 台北"),
                })),
                new Page(getString(R.string.tcbus), BusFragment.getInstance(new BusLineRequest[]{
                        new BusLineRequest("6187", "0", "1", "台中 -> 中正"),
                        new BusLineRequest("6187", "0", "2", "中正 -> 台中"),
                })),
                new Page(getString(R.string.train), TrainFragment.getInstance("1214")),

        });
        mPagerHelper.setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.setupWithViewPager(mViewPager);

        findViewById(R.id.switch_return).setOnClickListener(this);
    }

    @Override
    public void getBusState(String busNo, String branch, String isReturn, IOnUpdateListener<BusStop[]> listener) {
        IOnUpdateListener<BusStop[]> cacheListener = mCache.cache(
                String.format("%s_%s_%s_%s", KEY_BUS_PREFIX, busNo, branch, isReturn),
                listener,
                BusStop[].class,
                CACHE_TIME
        );

        if (cacheListener != null)
            BusStateSource.fetch(bus, cacheListener, busNo, branch, isReturn);
    }

    @Override
    public void getTrainStatus(String code, IOnUpdateListener<TrainTimetable> listener) {
        IOnUpdateListener<TrainTimetable> cacheListener = mCache.cache(String.format("%s_%s", KEY_TRAIN_PREFIX, code), listener, TrainTimetable.class, CACHE_TIME);

        if (cacheListener != null) {
            Date date = new Date();
            SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            TrainStopStatusSource.fetch(train, cacheListener, formater.format(date), code);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_return:
                Fragment fragment = mPagerHelper.getFragment(mViewPager.getCurrentItem());
                if (fragment != null) {
                    ((ISwitchLine) fragment).swtichLine();
                }
                break;
        }
    }

    @Override
    public CacheFragment cache() {
        return mCache;
    }

}
