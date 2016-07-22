package org.zankio.ccudata.ecourse.source.remote;

import android.support.annotation.NonNull;
import android.util.Log;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.source.http.HTTPJsoupSource;
import org.zankio.ccudata.base.utils.AnnotationUtils;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.annotation.ChangeCourse;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.CourseData;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class EcourseSource<TArgument extends CourseData, TData> extends HTTPJsoupSource<TArgument, TData> {
    public static final String IDENTIFY = "IDENTIFY";
    public static final String SESSION_ID = "SESSION_ID";
    public static final String SESSION_LOCK = "SESSION_LOCK";
    public static final String SESSION_COURSEID = "SESSION_COURSEID";
    private static final String REQUEST_COURSE = "REQUEST_COURSE";

    @Override
    public void initHTTPRequest(Request<TData, TArgument> request) {
        super.initHTTPRequest(request);
        httpParameter(request).cookies("PHPSESSID", restorageSession(getContext()));
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void before(Request<TData, TArgument> request) {
        super.before(request);

        Ecourse context = (Ecourse) getContext();
        String session = restorageSession(context);

        if (session == null)
            authenticate(context);

        if (needChangeCourse())
               changeCourse(context, getLock(context), getCourse(request));
    }

    @Override
    public void after(Response<TData, TArgument> response) {
        super.after(response);

        if (needChangeCourse()) {
            ReadWriteLock lock = getLock(getContext());
            lock.readLock().unlock();
        }
    }

    ////// Action //////

    public static void authenticate(@NonNull Ecourse context) {
        Log.d("EcourseSource", "authenticate");

        getLock(context).readLock().lock();
        String session = restorageSession(context);
        if (session == null) {
            getLock(context).readLock().unlock();
            getLock(context).writeLock().lock();
            try {
                context
                        .fetch(Authenticate.request(
                                context.user().username(),
                                context.user().password()
                        ))
                        .toBlocking()
                        .single();

                getLock(context).readLock().lock();
            } finally {
                getLock(context).writeLock().unlock();
            }
        }
        getLock(context).readLock().unlock();
    }

    public static void changeCourse(@NonNull Repository context, @NonNull ReadWriteLock lock, @NonNull Course course) {
        lock.readLock().lock();
        String courseID = getSessionCourseID(context);

        if (courseID == null || !courseID.equals(course.courseid)) {
            lock.readLock().unlock();
            lock.writeLock().lock();

            context.fetch(ChangeCourseSource.request(course)).toBlocking().last();
            lock.readLock().lock();
            lock.writeLock().unlock();
        }
    }

    public boolean needChangeCourse () {
        Boolean value = AnnotationUtils.getAnnotationValue(this.getClass(), ChangeCourse.class, false);
        return value == null ? false : value;
    }

    ////// Property //////

    public static void storageSession(Repository context, String session) {
        context.storage().put(SESSION_ID, session);
    }

    public static String restorageSession(Repository context) {
        return context.storage().get(SESSION_ID, String.class);
    }

    public static String getSessionCourseID(Repository context) {
        return context.storage().get(SESSION_COURSEID, String.class);
    }

    public static void setSessionCourseID(Repository context, String courseID) {
        context.storage().put(SESSION_COURSEID, courseID);
    }

    public static ReadWriteLock getLock(Repository context) {
        ReadWriteLock lock = context.storage().get(SESSION_LOCK, ReadWriteLock.class);
        if (lock == null) {
            lock = new ReentrantReadWriteLock();
            context.storage().put(SESSION_LOCK, lock);
        }
        return lock;
    }

    public static Course getCourse(Request<?, ? extends CourseData> request) {
        return request.args.course;
        //return request.storage().get(REQUEST_COURSE, Course.class);
    }

    /*public static void setCourse(Request request, Course course) {
        request.storage().put(REQUEST_COURSE, course);
    }*/
}
