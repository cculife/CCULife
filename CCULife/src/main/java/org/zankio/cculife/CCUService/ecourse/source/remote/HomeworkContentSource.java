package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.constant.Url;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.Homework;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

public class HomeworkContentSource extends BaseSource<Void> {
    public final static String TYPE = "HOMEWORK_CONTENT";
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

    public HomeworkContentSource(Ecourse context) {
        super(context, property);
    }

    public void parseHomeworkContent(Document document, Homework homework) throws Exception {
        Elements content;

        content = document.select("pre");
        if (content.size() == 0) throw new Exception("讀取作業題目錯誤");

        homework.content = content.html();
    }

    public static void fetch(Ecourse context, IOnUpdateListener listener, Course course, Homework homework) {
        context.fetch(TYPE, listener, course, homework);
    }

    @Override
    public Void fetch(String type, Object... arg) throws Exception {
        if (arg.length < 2) throw new Exception("arg is miss");

        Ecourse context = (Ecourse) this.context;
        BaseSession session = context.getSession();
        Course course = (Course) arg[0];
        Homework homework = (Homework) arg[1];
        if (session == null) throw new Exception("Session is miss");

        CourseSource.authenticate(context, session);

        ReadWriteLock lock = session.getLock();
        try {
            CourseSource.changeCourse(context, session, lock, course);

            Connection connection;

            connection = context.buildConnection(String.format(Url.COURSE_HOMEWORK_CONTENT, homework.id));
            connection.method(Connection.Method.GET);
            connection.followRedirects(false);

            connection.execute();
            String location = connection.response().header("location");

            if (location != null) {
                if (!location.startsWith("http")) {
                    homework.contentUrl = "http://ecourse.ccu.edu.tw/php/Testing_Assessment/" + location;
                } else {
                    homework.contentUrl = location;
                }
            } else {
                parseHomeworkContent(connection.response().parse(), homework);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        } finally {
            lock.readLock().unlock();
        }
    }
}
