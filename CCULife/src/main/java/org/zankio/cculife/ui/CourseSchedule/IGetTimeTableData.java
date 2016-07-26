package org.zankio.cculife.ui.CourseSchedule;

import org.zankio.ccudata.kiki.model.TimeTable;

import rx.subjects.BehaviorSubject;

public interface IGetTimeTableData {
    BehaviorSubject<TimeTable> getTimeTable();
}
