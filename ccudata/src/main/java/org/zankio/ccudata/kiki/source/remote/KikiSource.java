package org.zankio.ccudata.kiki.source.remote;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.User;
import org.zankio.ccudata.base.source.http.HTTPJsoupSource;
import org.zankio.ccudata.kiki.Kiki;

public abstract class KikiSource<TArgument, TData> extends HTTPJsoupSource<TArgument, TData> {
    public static final String IDENTIFY = "IDENTIFY";
    public static final String SESSION_ID = "SESSION_ID";
    private static final String SESSION_FIELD_NAME = "session_id";

    public static void storageSession(Repository context, String session) {
        context.storage().put(SESSION_ID, session);
    }

    public static String restorageSession(Repository context) {
        return context.storage().get(SESSION_ID, String.class);
    }

    @Override
    public void initHTTPRequest(Request<TData, TArgument> request) {
        super.initHTTPRequest(request);
        httpParameter(request)
                .queryStrings(SESSION_FIELD_NAME, restorageSession(getContext()));
    }

    @Override
    public void before(Request<TData, TArgument> request) {
        super.before(request);

        Kiki context = (Kiki) getContext();
        User user = context.user();
        String session = restorageSession(context);

        if (session == null)
            context.fetch(Authenticate.request(user.username(), user.password()))
                    .toBlocking()
                    .single();

    }
}
