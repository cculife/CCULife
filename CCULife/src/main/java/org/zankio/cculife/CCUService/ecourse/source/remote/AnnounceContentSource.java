package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Announce;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

public class AnnounceContentSource extends BaseSource<Announce> {
    public final static String TYPE = "ANNOUNCE_CONTENT";
    public final static String[] DATA_TYPES = { TYPE };
    public final static SourceProperty property;
    static  {
        property = new SourceProperty(
                SourceProperty.Level.MIDDLE,
                SourceProperty.Level.HIGH,
                false,
                DATA_TYPES
        );
    }

    public AnnounceContentSource(Ecourse context) {
        super(context, property);
    }

    public static void fetch(BaseRepo context, IOnUpdateListener listener, Announce announce) {
        context.fetch(TYPE, listener, announce);
    }

    public String parseAnnounceContent(Document document) throws Exception{
        Elements rows;

        rows = document.select("td[bgcolor=#E8E8E8]");

        if (rows.size() > 2 )
            return rows.get(2).html();
        else
            throw new Exception("讀取資料錯誤");

    }

    @Override
    public Announce fetch(String type, Object... arg) throws Exception {
        if (arg.length < 1) throw new Exception("arg is miss");

        Ecourse context = (Ecourse) this.context;
        BaseSession session = context.getSession();
        Announce announce = (Announce) arg[0];
        if (session == null) throw new Exception("Session is miss");

        CourseSource.authenticate(context, session);

        ReadWriteLock lock = session.getLock();
        try {
            CourseSource.changeCourse(context, session, lock, announce.getCourse());
            Connection connection = context.buildConnection(String.format(Url.COURSE_ANNOUNCE_CONTENT, announce.url));

            String result = parseAnnounceContent(connection.get());
            announce.content = result;
            return announce;

        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        }
    }
}
