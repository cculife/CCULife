package org.zankio.ccudata.base.model;

import java.io.IOException;
import java.util.List;

public abstract class HttpResponse {
    public abstract String url();
    public abstract List<String> headers(String name);
    public abstract String header(String name);
    public abstract String string() throws IOException;
    public abstract byte[] bytes() throws IOException;
    public abstract String cookie(String name);
}
