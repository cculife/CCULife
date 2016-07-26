package org.zankio.cculife.ui.base;


import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.model.Course;

public interface IGetCourseData {
    Ecourse getEcourse();
    Course getCourse(String id);
}
