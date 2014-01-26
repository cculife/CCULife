package org.zankio.cculife.CCUService.Source;

import org.zankio.cculife.CCUService.Kiki;
import org.zankio.cculife.SessionManager;

public abstract class KikiSource implements ISource{
    public abstract boolean Authenticate(SessionManager sessionManager) throws Exception;
    public abstract Kiki.TimeTable getTimeTable() throws Exception;
    public abstract Kiki.Course[] getCourseList(int year, int term) throws Exception;

}
