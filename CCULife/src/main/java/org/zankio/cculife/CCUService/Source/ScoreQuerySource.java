package org.zankio.cculife.CCUService.Source;

import org.zankio.cculife.CCUService.ScoreQuery;
import org.zankio.cculife.SessionManager;

public abstract class ScoreQuerySource extends BaseSource {
    public abstract boolean Authentication(SessionManager sessionManager) throws Exception;
    public abstract ScoreQuery.Grade[] getGrades() throws Exception;
}
