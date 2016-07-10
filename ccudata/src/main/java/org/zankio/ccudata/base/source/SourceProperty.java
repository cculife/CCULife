package org.zankio.ccudata.base.source;

public class SourceProperty {
    public enum Level { LOW, MIDDLE, HIGH }
    public final boolean isOffline;
    public final Level order;
    public final Level important;
    public final String[] DataList;

    public SourceProperty(Level order, Level important, boolean isOffline, String[] dataList) {
        this.isOffline = isOffline;
        this.order = order;
        this.important = important;
        this.DataList = dataList;
    }
}
