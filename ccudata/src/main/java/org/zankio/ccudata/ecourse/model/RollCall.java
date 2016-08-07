package org.zankio.ccudata.ecourse.model;

public class RollCall {
    public class Record {
        public boolean absent = false;
        public String date;
        public String comment;
    }

    public int attend = -1;
    public int absent = -1;
    public Record[] records;

}
