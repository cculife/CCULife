package org.zankio.cculife.ui.CourseSchedule;

import org.zankio.ccudata.kiki.model.TimeTable;

import rx.subjects.ReplaySubject;

public interface IGetTimeTableData {
    ReplaySubject<TimeTable> getTimeTable();
}
