package org.zankio.ccudata.train.source.remote;

import org.junit.Test;
import org.zankio.ccudata.train.model.TrainTimetable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrainStopStatusSourceTest {

    @Test
    public void testFetch() throws Exception {
        TrainTimetable timetable = new TrainStopStatusSource().fetch(
                TrainStopStatusSource.request(
                        new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()),
                        "1214"
                )
        );

        System.out.println("Uplink");

        for (TrainTimetable.Item train : timetable.up) {
            System.out.println(
                    String.format(
                            "%-4s %s\t%s \t%s %s",
                            train.trainNo,
                            train.departure,
                            train.trainType,
                            train.to,
                            train.delay
                    )
            );
        }

        System.out.println("Downlink");
        for (TrainTimetable.Item train : timetable.down) {
            System.out.println(
                    String.format(
                            "%-4s %s\t%s \t%s %s",
                            train.trainNo,
                            train.departure,
                            train.trainType,
                            train.to,
                            train.delay
                    )
            );
        }



    }
}