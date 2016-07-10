package org.zankio.ccudata.base.model;

import org.zankio.ccudata.base.source.BaseSource;

public class Request<TData, TArgument> {
    public TArgument args;
    public Class<? extends TData> target;
    public String type;
    private BaseSource source;

    public Request() { }
    public Request(Request<TData, TArgument> request) {
        this.source = request.source;
        this.target = request.target;
        this.type = request.type;
        this.args = request.args;
    }
    public Request(String type, TArgument args) {
        this.type = type;
        this.args = args;
    }
    public Request(String type, TArgument args, Class<? extends TData> target) {
        this.type = type;
        this.args = args;
        this.target = target;
    }

    public BaseSource source() { return this.source; }
    public Request<TData, TArgument> source(BaseSource source) { this.source = source; return this;}
}
