package org.zankio.ccudata.ecourse.source.remote;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.ccudata.base.constant.Exceptions;
import org.zankio.ccudata.base.model.HttpResponse;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.source.SourceProperty;
import org.zankio.ccudata.base.source.annotation.DataType;
import org.zankio.ccudata.base.source.annotation.Important;
import org.zankio.ccudata.base.source.annotation.Order;
import org.zankio.ccudata.base.source.http.annotation.Charset;
import org.zankio.ccudata.base.source.http.annotation.Method;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.constant.Urls;
import org.zankio.ccudata.ecourse.model.Announce;
import org.zankio.ccudata.ecourse.model.AnnounceData;
import org.zankio.ccudata.ecourse.model.Course;

@SuppressWarnings("DefaultAnnotationParam")

@Method("GET")
@Charset("big5")

@DataType(AnnounceContentSource.TYPE)
@Order(SourceProperty.Level.MIDDLE)
@Important(SourceProperty.Level.HIGH)
@ChangeCourse
public class AnnounceContentSource extends EcourseSource<AnnounceData, Announce> {
    public final static String TYPE = "ANNOUNCE_CONTENT";

    public static Request<Announce, AnnounceData> request(Course course, Announce announce) {
        return new Request<>(TYPE, new AnnounceData(announce, course), Announce.class);
    }

    @Override
    protected Announce parse(Request<Announce, AnnounceData> request, HttpResponse response, Document document) throws Exception {
        Announce announce = request.args.announce;
        announce.content = parseAnnounceContent(document);
        return announce;
    }

    public String parseAnnounceContent(Document document) throws Exception{
        Elements rows;

        rows = document.select("td[bgcolor=#E8E8E8]");

        if (rows.size() > 2 )
            return rows.get(2).html();
        else
            throw new Exception(Exceptions.PARSE_FAIL);

    }

    @Override
    public void initHTTPRequest(Request<Announce, AnnounceData> request) {
        super.initHTTPRequest(request);
        AnnounceData announceData = request.args;
        httpParameter(request)
                .url(String.format(Urls.COURSE_ANNOUNCE_CONTENT, announceData.announce.url));
    }
}
