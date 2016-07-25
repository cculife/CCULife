package org.zankio.ccudata.base.model;

import org.zankio.ccudata.base.source.BaseSource;

public class Request<TData, TArgument> {
    public TArgument args;
    public Class<? extends TData> target;
    public String type;
    private BaseSource<TArgument, TData> source;
    private Storage storage = new Storage();
    private Exception exception;

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

    public BaseSource<TArgument, TData> source() { return this.source; }
    public Request<TData, TArgument> source(BaseSource<TArgument, TData> source) { this.source = source; return this;}

    public Storage storage() { return storage; }

    public Request<TData, TArgument> exception(Exception e) {
        exception = e;
        return this;
    }

    public Exception exception() {
        return exception;
    }
}
