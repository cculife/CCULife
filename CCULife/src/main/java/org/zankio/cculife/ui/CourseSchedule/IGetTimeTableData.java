package org.zankio.cculife.ui.CourseSchedule;

import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.kiki.model.TimeTable;

public interface IGetTimeTableData {
    boolean getTimeTable(IOnUpdateListener<TimeTable> listener);
}
