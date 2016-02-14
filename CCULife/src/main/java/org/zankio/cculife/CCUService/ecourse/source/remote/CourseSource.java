package org.zankio.cculife.CCUService.ecourse.source.remote;

import android.support.annotation.NonNull;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.zankio.cculife.CCUService.base.BaseRepo;
import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

public abstract class CourseSource<T> extends Arg1Source<T, Course> {
    protected CourseSource(BaseRepo context, SourceProperty property) {
        super(context, property);
    }

    public static void authenticate(@NonNull Ecourse context, @NonNull BaseSession session) throws Exception {
        if (!session.isAuthenticated())
            context.fetchSync(Authenticate.TYPE, context.getUsername(), context.getPassword());
    }

    public static void changeCourse(@NonNull BaseRepo context, @NonNull BaseSession session, @NonNull ReadWriteLock lock, @NonNull Course course) throws Exception {
        lock.readLock().lock();
        if (session.getIdentity() == null || !session.getIdentity().equals(course.courseid)) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            context.fetchSync(ChangeCourseSource.TYPE, course);
            lock.readLock().lock();
            lock.writeLock().unlock();
        }
    }

    public Class<Course> getArg1Class() { return Course.class; }

    @Override
    public T _fetch(Course course) throws Exception{
        Ecourse context = (Ecourse) this.context;
        BaseSession session = context.getSession();
        if (session == null) throw new Exception("Session is miss");

        authenticate(context, session);

        ReadWriteLock lock = session.getLock();
        try {
            changeCourse(context, session, lock, course);

            Connection connection = context.buildConnection(getUrl(course));

            return parse(execute(connection), course);
        } catch (IOException e) {
            e.printStackTrace();
            throw Exceptions.getNetworkException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected Document execute(Connection connection) throws IOException { return connection.get(); };
}
