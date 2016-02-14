package org.zankio.cculife.CCUService.ecourse.source.remote;

import org.zankio.cculife.CCUService.base.BaseSession;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.base.source.SourceProperty;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.CCUService.ecourse.model.FileGroup;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;

public class FileSource extends BaseSource<FileGroup[]> {
    public final static String TYPE = "FILE";
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

    public FileSource(Ecourse context) {
        super(context, property);
    }

    public static void fetch(Ecourse context, IOnUpdateListener listener, Course course) {
        context.fetch(TYPE, listener, course);
    }

    @Override
    public FileGroup[] fetch(String type, Object... arg) throws Exception {
        if (arg.length < 1) throw new Exception("arg is miss");

        Ecourse context = (Ecourse) this.context;
        BaseSession session = context.getSession();
        Course course = (Course) arg[0];
        if (session == null) throw new Exception("Session is miss");

        CourseSource.authenticate(context, session);

        ReadWriteLock lock = session.getLock();
        try {
            CourseSource.changeCourse(context, session, lock, course);

            ArrayList<FileGroup> result = new ArrayList<>();

            context.fetchSync(FileGroupSource.TYPE, course, result);

            return result.toArray(new FileGroup[result.size()]);

        } finally {
            lock.readLock().unlock();
        }
    }
}
