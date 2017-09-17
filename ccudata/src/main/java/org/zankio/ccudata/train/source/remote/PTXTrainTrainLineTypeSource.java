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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")

@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@DataType(PTXTrainTrainLineTypeSource.TYPE)
public class PTXTrainTrainLineTypeSource extends HTTPJSONSource<TrainRequest, TrainTimetable>{
    public final static String TYPE = "TRAIN_LINE_TYPE";
    private static final String URL_TRAIN_DAILY_TIMETABLE = "https://ptx.transportdata.tw/MOTC/v2/Rail/TRA/DailyTimetable/%s";
    public static Request<TrainTimetable, TrainRequest> request(String no, String date) {
        return new Request<>(TYPE, new TrainRequest(no, date), TrainTimetable.class);
    }

    @Override
    public void initHTTPRequest(Request<TrainTimetable, TrainRequest> request) {
        super.initHTTPRequest(request);
        TrainRequest trainRequest = request.args;
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        httpParameter(request)
                .url(String.format(URL_TRAIN_DAILY_TIMETABLE, trainRequest.date))
                .queryStrings(
                        "$filter",
                        String.format(
                                "StopTimes/any(d:d/StationID eq '%s' and d/DepartureTime ge '%s')",
                                trainRequest.no,
                                simpleDateFormat.format(calendar.getTime())
                        )
                )
                .queryStrings("$format", "JSON");
    }

    @Override
    protected TrainTimetable parse(Request<TrainTimetable, TrainRequest> request, HttpResponse response, JSON json) throws JSONException {
        TrainTimetable trainTimetable = new TrainTimetable();
        List<TrainTimetable.Item> up = new ArrayList<>();
        List<TrainTimetable.Item> down = new ArrayList<>();

        JSONArray traininfos = json.array();
        for (int i = 0; i < traininfos.length(); i++) {
            JSONObject traininfo = traininfos.getJSONObject(i).getJSONObject("DailyTrainInfo");
            TrainTimetable.Item item = trainTimetable.new Item();
            item.trainNo = traininfo.getString("TrainNo");
            item.lineType = parseLineType(traininfo.getInt("TripLine"));

            if (traininfo.getInt("Direction") == 0) up.add(item);
            else down.add(item);
        }

        trainTimetable.up = up.toArray(new TrainTimetable.Item[0]);
        trainTimetable.down = down.toArray(new TrainTimetable.Item[0]);

        return trainTimetable;
    }

    private String parseTrainClassification(String trainClassificationName) {
        if (trainClassificationName == null) return "";
        if (trainClassificationName.contains("太魯閣")) return "太魯閣";
        if (trainClassificationName.contains("普悠瑪")) return "普悠瑪";
        if (trainClassificationName.contains("自強")) return "自強";
        if (trainClassificationName.contains("復興")) return "復興";
        if (trainClassificationName.contains("莒光")) return "莒光";
        if (trainClassificationName.contains("區間快")) return "區間快";
        if (trainClassificationName.contains("區間")) return "區間";
        return "";
    }

    private String parseLineType(int tripLine) {
        return tripLine == 0 ? "" :
                tripLine == 1 ? "山線" : "海線";
    }

    private String parseDelay(String delay) {
        if (delay == null || "".equals(delay)) return "";
        else if ("0".equals(delay)) return "準點";
        else return String.format("晚 %s 分", delay);
    }

}
