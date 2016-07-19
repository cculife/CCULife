package org.zankio.ccudata.train.model;

public class TrainRequest {
    public String date; // 2017/01/01
    public String no; // 1214

    public TrainRequest(String date, String no) {
        this.date = date;
        this.no = no;
    }
}
