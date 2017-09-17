package org.zankio.ccudata.train.source.remote;

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
import org.zankio.ccudata.train.model.TrainRequest;
import org.zankio.ccudata.train.model.TrainTimetable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")

@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@DataType(PTXTrainLiveDelaySource.TYPE)
public class PTXTrainLiveDelaySource extends HTTPJSONSource<TrainRequest, TrainTimetable>{
    public final static String TYPE = "TRAIN_LIVE_DELAY";
    private static final String URL_TRAIN_DELAY = "https://ptx.transportdata.tw/MOTC/v2/Rail/TRA/LiveBoard/%s";
    private static final Map<String, String> trainClassification = new HashMap<>();
    static {
        trainClassification.put("1115", "莒光");
        trainClassification.put("12A1", "單機迴送");
        trainClassification.put("1108", "自強");
        trainClassification.put("1100", "自強");
        trainClassification.put("1101", "自強");
        trainClassification.put("1102", "太魯閣");
        trainClassification.put("1107", "普悠瑪");
        trainClassification.put("1110", "莒光");
        trainClassification.put("1120", "復興");
        trainClassification.put("1130", "電車");
        trainClassification.put("1131", "區間車");
        trainClassification.put("1132", "區間快");
        trainClassification.put("1140", "普快車");
        trainClassification.put("1141", "柴快車");
        trainClassification.put("1150", "柴油車");
        trainClassification.put("1154", "柴客");
        trainClassification.put("12A0", "調車列車");
        trainClassification.put("1152", "行包專車");
        trainClassification.put("1282", "臨時客迴");
        trainClassification.put("1104", "自強");
        trainClassification.put("1106", "自強");
        trainClassification.put("1281", "柴迴");
        trainClassification.put("1270", "普通貨車");
        trainClassification.put("1112", "莒光");
        trainClassification.put("1111", "莒光");
        trainClassification.put("1155", "柴客");
        trainClassification.put("1135", "區間車");
        trainClassification.put("1103", "自強");
        trainClassification.put("1122", "復興");
        trainClassification.put("1113", "莒光");
        trainClassification.put("12B0", "試轉運");
        trainClassification.put("1134", "兩鐵");
        trainClassification.put("4200", "特種");
        trainClassification.put("1133", "電車");
        trainClassification.put("1151", "普通車");
        trainClassification.put("5230", "特種");
        trainClassification.put("1280", "客迴");
        trainClassification.put("1105", "自強");
        trainClassification.put("1121", "復興");
        trainClassification.put("1114", "莒光");
    }

    public static Request<TrainTimetable, TrainRequest> request(String no) {
        return new Request<>(TYPE, new TrainRequest(no), TrainTimetable.class);
    }

    @Override
    public void initHTTPRequest(Request<TrainTimetable, TrainRequest> request) {
        super.initHTTPRequest(request);
        TrainRequest trainRequest = request.args;
        httpParameter(request)
                .url(String.format(URL_TRAIN_DELAY, trainRequest.no))
                .queryStrings("$format", "JSON");
    }

    @Override
    protected TrainTimetable parse(Request<TrainTimetable, TrainRequest> request, HttpResponse response, JSON json) throws JSONException {
        TrainTimetable trainTimetable = new TrainTimetable();
        List<TrainTimetable.Item> up = new ArrayList<>();
        List<TrainTimetable.Item> down = new ArrayList<>();

        JSONArray traininfos = json.array();
        for (int i = traininfos.length() - 1; i >= 0; i--) {
            JSONObject traininfo = traininfos.getJSONObject(i);
            TrainTimetable.Item item = trainTimetable.new Item();
            item.trainNo = traininfo.getString("TrainNo");
            item.lineType = parseLineType(traininfo.getInt("TripLine"));
            item.delay = parseDelay(traininfo.getString("DelayTime"));
            item.to = traininfo.getJSONObject("EndingStationName").getString("Zh_tw");
            item.departure = traininfo.getString("ScheduledDepartureTime").substring(0, 5);
            item.trainType = parseTrainClassification(traininfo.getString("TrainClassificationID"));

            if (traininfo.getInt("Direction") == 0) up.add(item);
            else down.add(item);
        }

        trainTimetable.up = up.toArray(new TrainTimetable.Item[0]);
        trainTimetable.down = down.toArray(new TrainTimetable.Item[0]);

        return trainTimetable;
    }

    private String parseTrainClassification(String trainClassificationID) {
        return trainClassification.get(trainClassificationID);
    }

    private String parseLineType(int tripLine) {
        return tripLine == 0 ? "" :
                tripLine == 1 ? "山" : "海";
    }

    private String parseDelay(String delay) {
        if (delay == null || "".equals(delay)) return "";
        else if ("0".equals(delay)) return "準點";
        else return String.format("晚 %s 分", delay);
    }

}
