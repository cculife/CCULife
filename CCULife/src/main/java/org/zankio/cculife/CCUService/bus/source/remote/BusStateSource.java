package org.zankio.cculife.CCUService.bus.source.remote;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.bus.Bus;
import org.zankio.cculife.CCUService.bus.model.BusStop;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;

public class BusStateSource extends BaseSource<BusStop[]> {
    public final static String TYPE = "BUS_STATE";
    public final static String[] DATA_TYPES = { TYPE };
    public final static SourceProperty property;
    private static final String URL_BUS_PREDICTION_TIME = "http://www.taiwanbus.tw/app_api/SP_PredictionTime_N.ashx?routeNo=%s&branch=%s&goBack=%s&Source=w";

    static  {
        property = new SourceProperty(
                SourceProperty.Level.MIDDLE,
                SourceProperty.Level.HIGH,
                false,
                DATA_TYPES
        );
    }

    public BusStateSource(BaseRepo context) {
        super(context, property);
    }

    public BusStop[] parse(String body) {
        try {
            JSONArray busList = new JSONArray(body);
            BusStop[] result = new BusStop[busList.length()];

            for (int i = 0; i < busList.length(); i++) {
                JSONObject row = busList.getJSONObject(i);
                result[i] = new BusStop();
                result[i].id = row.getString("stopId");
                result[i].name = row.getString("name");
                result[i].seq = row.getString("seq");
                result[i].carNo = row.getString("carNo");
                result[i].perdiction = row.getString("predictionTime");
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new BusStop[0];
    }

    public static void fetch (
            @NonNull BaseRepo context,
            IOnUpdateListener listener,
            String busNo, String branch, String isReturn
    ) {

        context.fetch(BusStateSource.TYPE, listener, busNo, branch, isReturn);
    }

    @Override
    public BusStop[] fetch(String type, Object... arg) throws Exception {
        Bus context = (Bus) this.context;

        try {
            String busNo = (String) arg[0]; // 7309
            String branch = (String) arg[1]; // or A
            String isReturn = (String) arg[2]; // 1 or 2

            Connection connection = context.buildConnection(
                    String.format(URL_BUS_PREDICTION_TIME, busNo, branch, isReturn))
                    .ignoreContentType(true);

            return parse(connection.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}
