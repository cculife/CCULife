package org.zankio.ccudata.train.model;

public class TrainTimetable {

    public Item[] up;
    public Item[] down;

    public class Item {
        public String type;
        public String code;
        public String departure;
        public String delay;
        public String to;
        public String lineType;
    }
}
