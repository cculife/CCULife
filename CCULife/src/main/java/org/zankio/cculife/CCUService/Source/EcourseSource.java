package org.zankio.cculife.CCUService.Source;

import org.zankio.cculife.CCUService.Ecourse;
import org.zankio.cculife.SessionManager;

public abstract class EcourseSource implements ISource {

    public abstract boolean Authentication(SessionManager sessionManager) throws Exception;

    public abstract void switchCourse(Ecourse.Course course);

    public abstract Ecourse.Course[] getCourse() throws Exception;

    public abstract Ecourse.Scores[] getScore() throws Exception;

    public abstract Ecourse.Classmate[] getClassmate() throws Exception;

    public abstract Ecourse.Announce[] getAnnounces(Ecourse.Course course) throws Exception;

    public abstract String getAnnounceContent(Ecourse.Announce announce) throws Exception;

    public abstract Ecourse.File[] getFiles(Ecourse.Course course) throws Exception ;

}
