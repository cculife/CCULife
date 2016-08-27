package org.zankio.cculife.ui.course.schedule;

import org.zankio.ccudata.kiki.model.TimeTable;

import rx.subjects.ReplaySubject;

public interface IGetTimeTableData {
    ReplaySubject<TimeTable> getTimeTable();
}
