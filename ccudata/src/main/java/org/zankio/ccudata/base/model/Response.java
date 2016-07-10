package org.zankio.ccudata.base.model;

public class Response<TData, TArgument> {
    private Request<TData, TArgument> args;
    private TData data;
    private Exception exception;
    public Response() {}
    public Response(TData data, Request<TData, TArgument> args) { this.data = data; this.args = args; }
    public Response(Exception e, Request<TData, TArgument> args) { this.exception = e; this.args = args; }
    public Request<TData, TArgument> args() { return this.args; }
    public Response<TData, TArgument> args(Request<TData, TArgument> args) { this.args = args; return this; }
    public TData data() { return this.data; }
    public Exception exception() { return exception; }
    public Response<TData, TArgument> data(TData data) { this.data = data; return this; }
    //public <TD>Response<TD, TArgument> toData(TD data) { return new Response<>(data, this.args); }
}
