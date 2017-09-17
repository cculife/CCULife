package org.zankio.ccudata.bus.source.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.JSON;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.HTTPJSONSource;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.bus.model.BusLineRequest;
import org.zankio.ccudata.bus.model.BusStop;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")

@DataType(BusStateSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
public class BusStateSource extends HTTPJSONSource<BusLineRequest, BusStop[]> {
    public static final String TYPE = "BUS_STATE";
    private static final String URL_BUS_PREDICTION_TIME =
            "http://www.taiwanbus.tw/app_api/SP_PredictionTime_N.ashx" +
                    "?routeNo=%s" +
                    "&branch=%s" +
                    "&goBack=%s" +
                    "&Source=w";

/*    public BusStop[] parse(String body) {
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
                result[i].perdiction = row.getString("predictionTime").replaceAll("<a[^>]*>([^<]*)</a>", "$1");
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new BusStop[0];
    }*/

    @Override
    public void initHTTPRequest(Request<BusStop[], BusLineRequest> request) {
        super.initHTTPRequest(request);
        BusLineRequest busLineData = request.args;
        httpParameter(request)
                .url(
                        String.format(
                                URL_BUS_PREDICTION_TIME,
                                busLineData.busNo,
                                busLineData.branch,
                                busLineData.isReturn
                        )
                );
    }

    @Override
    protected BusStop[] parse(Request<BusStop[], BusLineRequest> request, HttpResponse response, JSON json) throws JSONException {
        try {
            JSONArray busList = json.array();
            BusStop[] result = new BusStop[busList.length()];

            for (int i = 0; i < busList.length(); i++) {
                JSONObject row = busList.getJSONObject(i);
                result[i] = new BusStop();
                result[i].id = row.getString("stopId");
                result[i].name = row.getString("name");
                result[i].seq = row.getString("seq");
                result[i].carNo = row.getString("carNo");
                result[i].perdiction = row.getString("predictionTime").replaceAll("<a[^>]*>([^<]*)</a>", "$1");
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new BusStop[0];
    }

    public static Request<BusStop[], BusLineRequest> request(String busNo, String branch, String isReturn) {
        return new Request<>(TYPE, new BusLineRequest(busNo, branch, isReturn, ""), BusStop[].class);
    }

}
