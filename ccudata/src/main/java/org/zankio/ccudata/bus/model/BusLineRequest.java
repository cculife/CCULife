package org.zankio.ccudata.bus.model;

import java.util.ArrayList;
import java.util.List;

public class BusLineRequest {
    public String busNo;
    public String branch;
    public String isReturn;
    public String label;

    public BusLineRequest(String busNo, String branch, String isRetuen, String lable) {
        this.busNo = busNo;
        this.branch = branch;
        this.isReturn = isRetuen;
        this.label = lable;
    }


    public String toString() {
        return String.format("%s###%s###%s###%s", busNo, branch, isReturn, label);
    }

    public static BusLineRequest fromString(String content) {
        String[] field;
        if (content == null) return null;

        field = content.split("###");
        if (field.length < 4) return null;

        return new BusLineRequest(field[0], field[1], field[2], field[3]);
    }

    public static BusLineRequest[] fromStringArray(String[] array) {
        List<BusLineRequest> result = new ArrayList<>();
        BusLineRequest request;
        for (String line : array) {
            request = fromString(line);
            if (request != null) result.add(request);

        }

        return result.toArray(new BusLineRequest[result.size()]);
    }
}
