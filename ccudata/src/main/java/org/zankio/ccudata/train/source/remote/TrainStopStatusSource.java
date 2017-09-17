package org.zankio.ccudata.train.source.remote;

import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.HTTPStringSource;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.train.model.TrainRequest;
import org.zankio.ccudata.train.model.TrainTimetable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")

@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@DataType(TrainStopStatusSource.TYPE)
public class TrainStopStatusSource extends HTTPStringSource<TrainRequest, TrainTimetable>{
    public final static String TYPE = "TRAIN_STOP_STATE";
    private static final String URL_TRAIN_STATUS = "http://twtraffic.tra.gov.tw/twrail/mobile/StationSearchResult.aspx?searchdate=%s&fromstation=%s";

    public static Request<TrainTimetable, TrainRequest> request(String no, String date) {
        return new Request<>(TYPE, new TrainRequest(no, date), TrainTimetable.class);
    }

    @Override
    public void initHTTPRequest(Request<TrainTimetable, TrainRequest> request) {
        super.initHTTPRequest(request);
        TrainRequest trainRequest = request.args;
        httpParameter(request)
                .url(String.format(URL_TRAIN_STATUS, trainRequest.date, trainRequest.no));
    }

    @Override
    protected TrainTimetable parse(Request<TrainTimetable, TrainRequest> request, HttpResponse response, String body) throws Exception {
        // TRSearchResult.push('區間車');
        // TRSearchResult.push('2318');
        // TRSearchResult.push('06:00');
        // TRSearchResult.push('臺中');
        // TRSearchResult.push('0');
        // TRSearchResult.push('0');
        // TRSearchResult.push('1131');

        Pattern pattern = Pattern.compile(
                "TRSearchResult\\.push\\('([^']*)'\\);" +
                        "TRSearchResult\\.push\\('([^']*)'\\);" +
                        "TRSearchResult\\.push\\('([^']*)'\\);" +
                        "TRSearchResult\\.push\\('([^']*)'\\);" +
                        "TRSearchResult\\.push\\('([^']*)'\\);" +
                        "TRSearchResult\\.push\\('([^']*)'\\);" +
                        "TRSearchResult\\.push\\('([^']*)'\\);");

        Matcher matcher = pattern.matcher(body);

        TrainTimetable timetable = new TrainTimetable();
        List<TrainTimetable.Item> up = new ArrayList<>();
        List<TrainTimetable.Item> down = new ArrayList<>();
        while (matcher.find()) {
            TrainTimetable.Item item = timetable.new Item();
            item.trainType = matcher.group(1);
            item.trainNo = matcher.group(2);
            item.departure = matcher.group(3);
            item.to = matcher.group(4);
            item.delay = parseDelay(matcher.group(6));

            if ("0".equals(matcher.group(5))) up.add(item);
            else down.add(item);
        }

        timetable.up = up.toArray(new TrainTimetable.Item[up.size()]);
        timetable.down = down.toArray(new TrainTimetable.Item[down.size()]);
        return timetable;
    }

    public String parseDelay(String delay) {
        if (delay == null || "".equals(delay)) return "";
        else if ("0".equals(delay)) return "準點";
        else return String.format("晚 %s 分", delay);
    }
}
