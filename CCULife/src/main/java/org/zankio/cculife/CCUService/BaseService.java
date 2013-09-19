package org.zankio.cculife.CCUService;

public abstract class BaseService {
    public boolean inited = false;
    public String SESSIONID = null;

    public void init() throws Exception {
        if (!inited) getSession();
        inited = true;
    }

    public abstract boolean getSession() throws Exception;
}
