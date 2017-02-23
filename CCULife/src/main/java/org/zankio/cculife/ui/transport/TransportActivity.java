package org.zankio.cculife.ui.transport;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.bus.Bus;
import org.zankio.ccudata.bus.model.BusLineRequest;
import org.zankio.ccudata.bus.model.BusStop;
import org.zankio.ccudata.bus.source.remote.BusStateSource;
import org.zankio.ccudata.train.Train;
import org.zankio.ccudata.train.model.TrainRequest;
import org.zankio.ccudata.train.model.TrainTimetable;
import org.zankio.ccudata.train.source.remote.PTXTrainLiveDelaySource;
import org.zankio.ccudata.train.source.remote.PTXTrainStationTimetableSource;
import org.zankio.ccudata.train.source.remote.PTXTrainTrainLineTypeSource;
import org.zankio.ccudata.train.source.remote.TrainStopStatusSource;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseActivity;
import org.zankio.cculife.ui.base.CacheFragment;
import org.zankio.cculife.ui.base.IGetCache;
import org.zankio.cculife.ui.base.helper.FragmentPagerHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import rx.Observable;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                })),
                new Page(getString(R.string.tcbus), BusFragment.getInstance(new BusLineRequest[]{
                        new BusLineRequest("6187", "0", "1", "台中 -> 中正"),
                        new BusLineRequest("6187", "0", "2", "中正 -> 台中"),
                })),
                new Page(getString(R.string.train_minxiong), TrainFragment.getInstance("1214")),
                new Page(getString(R.string.train_chiayi), TrainFragment.getInstance("1215")),

        });
        mPagerHelper.setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.setupWithViewPager(mViewPager);

        findViewById(R.id.switch_return).setOnClickListener(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Observable<Response<BusStop[], BusLineRequest>> getBusState(String busNo, String branch, String isReturn) {
        String key = String.format("%s_%s_%s_%s", KEY_BUS_PREFIX, busNo, branch, isReturn);
        Observable<Response<BusStop[], BusLineRequest>> observable;
        observable = mCache.get(key, Observable.class);

        if (observable == null) {
            observable = bus.fetch(BusStateSource.request(busNo, branch, isReturn)).cache();
            mCache.set(key, observable, CACHE_TIME);
        }

        return observable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Observable<Response<TrainTimetable, TrainRequest>> getTrainStatus(String code) {
        String key = String.format("%s_%s", KEY_TRAIN_PREFIX, code);
        Observable<Response<TrainTimetable, TrainRequest>> observable;
        observable = mCache.get(key, Observable.class);

        if (observable == null) {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat formatWeb = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            Observable<Response<TrainTimetable, TrainRequest>> web = train.fetch(TrainStopStatusSource.request(code, formatWeb.format(date)));
            observable =
                    train.fetch(PTXTrainStationTimetableSource.request(code, format.format(date)))
                            .concatWith(train.fetch(PTXTrainTrainLineTypeSource.request(code, format.format(date))))
                            .concatWith(train.fetch(PTXTrainLiveDelaySource.request(code)))
                            .reduce((r1, r2) -> {
                                Log.d("reduce", r1.request().type + "/" + r2.request().type);
                                TrainTimetable timetable = r1.data();
                                TrainTimetable timetableDelay = r2.data();
                                Map<String, TrainTimetable.Item> additionData = new HashMap<>();

                                for (TrainTimetable.Item i : timetableDelay.up)
                                    additionData.put(i.trainNo, i);

                                for (TrainTimetable.Item i : timetableDelay.down)
                                    additionData.put(i.trainNo, i);

                                for (TrainTimetable.Item i : timetable.up) {
                                    TrainTimetable.Item item = additionData.get(i.trainNo);
                                    if (item == null) continue;

                                    if (i.delay == null) i.delay = item.delay;
                                    if (i.lineType == null) i.lineType = item.lineType;
                                }

                                for (TrainTimetable.Item i : timetable.down) {
                                    TrainTimetable.Item item = additionData.get(i.trainNo);
                                    if (item == null) continue;

                                    if (i.delay == null) i.delay = item.delay;
                                    if (i.lineType == null) i.lineType = item.lineType;
                                }

                                return r1.data(timetable);
                            })
                            .onErrorResumeNext(web)
                            .cache();
            mCache.set(key, observable, CACHE_TIME);
        }
        return observable;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_return:
                Fragment fragment = mPagerHelper.getFragment(mViewPager.getCurrentItem());
                if (fragment != null) {
                    ((ISwitchLine) fragment).switchLine();
                }
                break;
        }
    }

    @Override
    public CacheFragment cache() {
        return mCache;
    }

}
