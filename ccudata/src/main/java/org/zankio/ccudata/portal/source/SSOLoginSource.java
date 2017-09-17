package org.zankio.ccudata.portal.source;

import org.jsoup.nodes.Document;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.User;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.HTTPJsoupSource;
import org.zankio.ccudata.base.source.http.annotation.FollowRedirect;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.portal.Portal;
import org.zankio.ccudata.portal.model.PortalData;


@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@FollowRedirect(false)

@DataType(SSOLoginSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
public class SSOLoginSource extends HTTPJsoupSource<PortalData, String> {
    public static final String TYPE = "SSO_LOGIN";

    public static Request<String, PortalData> request(PortalData data) {
        return new Request<>(TYPE, data, String.class);
    }

    @Override
    public void before(Request<String, PortalData> request) {
        super.before(request);

        Portal context = (Portal) getContext();
        String session = context.storage().get(Authenticate.SSO_SESSION_ID);
        User user = context.user();

        if (session == null)
            context
                    .fetch(
                            Authenticate.request(
                                    user.username(),
                                    user.password()
                            )
                    )
                    .toBlocking()
                    .single();

    }

    @Override
    public void initHTTPRequest(Request<String, PortalData> request) {
        super.initHTTPRequest(request);
        PortalData portalData = request.args;

        httpParameter(request)
                .cookies("ccuSSO", context.storage().<String>get(Authenticate.SSO_SESSION_ID))
                .url(portalData.getSSOPortalURL());
    }

    @Override
    protected String parse(Request<String, PortalData> request, HttpResponse response, Document document) throws Exception {
        return response.header("Location");
    }
}
