package org.zankio.cculife.ui.transport;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.bus.model.BusLineRequest;
import org.zankio.ccudata.bus.model.BusStop;
import org.zankio.cculife.R;
import org.zankio.cculife.ui.base.BaseMessageFragment;
import org.zankio.cculife.ui.base.IGetCache;
import org.zankio.cculife.utils.ExceptionUtils;

import rx.Subscriber;


public class BusFragment extends BaseMessageFragment implements ISwitchLine {
    private static final String KEY_CURRENT_LINE = "KEY_CURRENT_LINE";
    private static final String KEY_BUS = "KEY_BUS";

    private static final int ICON_MID = 1;
    private static final int ICON_FIRST = 2;
    private static final int ICON_LAST = 3;

    private IGetBusData context;
    private BusStopAdapter adapter;
    private BusLineRequest[] mRequest;
    private int mCurrentLine = -1;
    private IGetCache cacheContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.context = (IGetBusData) context;
            this.cacheContext = (IGetCache) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IGetBusData");
        }
    }

    public int currentLine() {
        if (mCurrentLine != -1) return mCurrentLine;
        Integer value = cacheContext.cache().get(KEY_CURRENT_LINE + request()[0].toString(), Integer.class);
        if (value != null) mCurrentLine = value;
        else mCurrentLine = 0;
        return mCurrentLine;
    }

    @Override
    public void onPause() {
        super.onPause();
        cacheContext.cache().set(KEY_CURRENT_LINE + request()[0].toString(), mCurrentLine);
    }

    public static Fragment getInstance(BusLineRequest requests) {
        return getInstance(new BusLineRequest[]{ requests });
    }

    public static Fragment getInstance(BusLineRequest[] requests) {
        BusFragment fragment = new BusFragment();
        fragment.setArguments(getArguments(requests));
        return fragment;
    }

    public static Bundle getArguments(BusLineRequest[] requests) {
        Bundle arguments = new Bundle();
        arguments.putStringArray(KEY_BUS, toStringArray(requests));
        return arguments;
    }

    private static <F>String[] toStringArray(F[] objects) {
        String[] result = new String[objects.length];

        for (int i = 0; i < objects.length; i++) {
            result[i] = objects[i].toString();
        }

        return result;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus, container, false);
    }

    public BusLineRequest[] request() {
        if (mRequest != null) return mRequest;
        Bundle arguments = getArguments();
        mRequest = BusLineRequest.fromStringArray(arguments.getStringArray(KEY_BUS));
        return mRequest;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new BusStopAdapter();
        View footer = View.inflate(getContext(), R.layout.transport_list_footer, null);
        ((TextView) footer.findViewById(R.id.footer_text)).setText(R.string.reference_bus);

        ListView listView = (ListView) view.findViewById(R.id.list);
        listView.addFooterView(footer, null, false);

        updateBus();
    }

    public void updateBus() {
        View view = getView();
        BusLineRequest[] requests = request();
        if (view != null && requests.length > currentLine()) {
            BusLineRequest request = requests[currentLine()];

            ((TextView) view.findViewById(R.id.bus_line)).setText(request.label);
            ListView listView = (ListView) view.findViewById(R.id.list);
            listView.setAdapter(adapter);

            message().show("讀取中...", true);
            Subscriber<Response<BusStop[], BusLineRequest>> subscriber = getNewListener(request.toString());
            context.getBusState(request.busNo, request.branch, request.isReturn).subscribe(subscriber);
        }
    }

    public Subscriber<Response<BusStop[], BusLineRequest>> getNewListener(final String bus_id) {
        return new Subscriber<Response<BusStop[],BusLineRequest>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (isCurrent()) return;
                e = ExceptionUtils.extraceException(e);

                message().show(e.getMessage());
            }

            @Override
            public void onNext(Response<BusStop[], BusLineRequest> response) {
                if (isCurrent()) return;
                adapter.setBusStops(response.data());
                adapter.notifyDataSetChanged();
                message().hide();
            }

            public boolean isCurrent() {
                int currentLine = currentLine();
                return mRequest.length > currentLine && !bus_id.equals(mRequest[currentLine].toString());
            }
        };
    }

    @Override
    public void switchLine() {
        BusLineRequest[] requests = request();
        mCurrentLine = (currentLine() + 1) % requests.length;
        adapter.setBusStops(new BusStop[0]);
        adapter.notifyDataSetChanged();

        updateBus();
    }

    private class BusStopAdapter extends BaseAdapter {
        private BusStop[] busStops;

        public void setBusStops(BusStop[] busStops) {
            this.busStops = busStops;
        }

        @Override
        public int getCount() {
            return busStops == null ? 0 : busStops.length;
        }

        @Override
        public Object getItem(int position) {
            return busStops == null ? null : busStops[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater
                        .from(getContext())
                        .inflate(R.layout.item_bus_stop, parent, false);

            BusStop busStop = busStops[position];
            int icon;
            if (position == 0) icon = ICON_FIRST;
            else if (position == busStops.length - 1) icon = ICON_LAST;
            else icon = ICON_MID;

            ((TextView) convertView.findViewById(R.id.name)).setText(busStop.name);
            ((TextView) convertView.findViewById(R.id.car_no)).setText(busStop.carNo);
            ((TextView) convertView.findViewById(R.id.prediction)).setText(busStop.perdiction);
            ((ImageView) convertView.findViewById(R.id.bus_stop_icon)).getDrawable().setLevel(icon);
            return convertView;
        }
    }
}
