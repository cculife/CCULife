package org.zankio.ccudata.train.model;

public class TrainStopRequest {
    public String date; // 2017/01/01
    public String no; // 1214
    public String direction; // 0 up / 1 down

    public TrainStopRequest(String date, String no, String direction) {
        this.date = date;
        this.no = no;
        this.direction = direction;
    }
}
