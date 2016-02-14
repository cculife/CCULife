package org.zankio.cculife.ui.base;

import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;

public interface IGetCourseData {
    Ecourse getEcourse();
    Course getCourse(String id);
}
