package org.zankio.ccudata.train.model;

public class TrainRequest {
    public String no; // 1214
    public String date; // 2017-01-01

    public TrainRequest(String no) {
        this(no, "");
    }

    public TrainRequest(String no, String date) {
        this.no = no;
        this.date = date;
    }
}
