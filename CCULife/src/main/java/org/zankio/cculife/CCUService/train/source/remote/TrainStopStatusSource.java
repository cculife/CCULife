package org.zankio.cculife.CCUService.train.source.remote;

import org.jsoup.Connection;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.train.model.TrainTimetable;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrainStopStatusSource extends BaseSource<TrainTimetable>{
    public final static String TYPE = "TRAIN_STOP_STATE";
    public final static String[] DATA_TYPES = { TYPE };
    private static final String URL_TRAIN_STATUS = "http://twtraffic.tra.gov.tw/twrail/mobile/StationSearchResult.aspx?searchdate=%s&fromstation=%s";

    public TrainStopStatusSource(BaseRepo context) {
        super(context,
                new SourceProperty(
                        SourceProperty.Level.MIDDLE,
                        SourceProperty.Level.HIGH,
                        false,
                        DATA_TYPES
                )
        );
    }

    public TrainTimetable parse(String body) {
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
            item.type = matcher.group(1);
            item.code = matcher.group(2);
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

    public static void fetch(BaseRepo context, IOnUpdateListener<TrainTimetable> listener, String date, String no) {
        context.fetch(TYPE, listener, date, no);
    }

    @Override
    public TrainTimetable fetch(String type, Object... arg) throws Exception {
        if (arg.length < 2) throw new IllegalArgumentException("arg is miss");
        try {
            String date = (String) arg[0]; // 2017/01/01
            String no = (String) arg[1]; // 1214

            Connection connection = context.buildConnection(
                    String.format(URL_TRAIN_STATUS, date, no))
                    .ignoreContentType(true);

            return parse(connection.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}
