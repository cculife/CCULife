package org.zankio.cculife.CCUService.train.source.remote;

import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.Test;
import org.zankio.cculife.CCUService.train.Train;
import org.zankio.cculife.CCUService.train.model.TrainTimetable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrainStopStatusTest extends AndroidTestCase{
    Train train = new Train(getContext());
    TrainStopStatusSource source = new TrainStopStatusSource(train);
    @Test
    public void testFetch() throws Exception {
        Date date = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        TrainTimetable timetable = source.fetch(TrainStopStatusSource.TYPE, formater.format(date), "1214");
        for (TrainTimetable.Item item: timetable.up) {
            Log.d("Train", String.format("%s %s %s %s %s",
                    item.code,
                    item.type,
                    item.to,
                    item.departure,
                    item.delay));
        }
        for (TrainTimetable.Item item: timetable.down) {
            Log.d("Train", String.format("%s %s %s %s %s",
                    item.code,
                    item.type,
                    item.to,
                    item.departure,
                    item.delay));
        }

    }
}