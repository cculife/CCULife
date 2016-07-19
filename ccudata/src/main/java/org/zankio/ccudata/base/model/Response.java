package org.zankio.ccudata.base.model;

public class Response<TData, TArgument> {
    private Request<TData, TArgument> request;
    private TData data;
    private Exception exception;
    public Response() {}
    public Response(TData data, Request<TData, TArgument> request) { this.data = data; this.request = request; }
    public Response(Exception e, Request<TData, TArgument> request) { this.exception = e; this.request = request; }
    public Request<TData, TArgument> request() { return this.request; }
    public Response<TData, TArgument> request(Request<TData, TArgument> args) { this.request = args; return this; }
    public TData data() { return this.data; }
    public Exception exception() { return exception; }
    public Response<TData, TArgument> data(TData data) { this.data = data; return this; }
    //public <TD>Response<TD, TArgument> toData(TD data) { return new Response<>(data, this.args); }
}
