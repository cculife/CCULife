package org.zankio.ccudata.train.model;

public class TrainTimetable {

    public Item[] up;
    public Item[] down;

    public class Item {
        public String trainType; // TrainClassificationID
        public String trainNo; // TrainNo
        public String departure; // ScheduledDepartureTime
        public String delay; // DelayTime
        public String to; // EndingStationName.Zh_tw
        public String lineType; // TripLine
    }
}
