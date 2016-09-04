package org.zankio.ccudata.ecourse.model;

import org.zankio.ccudata.base.model.OfflineMode;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.ecourse.Ecourse;
import org.zankio.ccudata.ecourse.source.remote.AnnounceSource;
import org.zankio.ccudata.ecourse.source.remote.ClassmateSource;
import org.zankio.ccudata.ecourse.source.remote.FileGroupSource;
import org.zankio.ccudata.ecourse.source.remote.HomeworkSource;
import org.zankio.ccudata.ecourse.source.remote.RollCallSource;
import org.zankio.ccudata.ecourse.source.remote.ScoreSource;

import rx.Observable;

import static org.zankio.ccudata.base.utils.RxJavaUtils.cache;


public class Course {
    public Observable<Response<ScoreGroup[], CourseData>> loadingScore;
    public Observable<Response<RollCall , CourseData>> loadingRollCall;
    public Observable<Response<Homework[]  , CourseData>> loadingHomework;
    public Observable<Response<FileGroup[] , CourseData>> loadingFiles;
    public Observable<Response<Classmate[] , CourseData>> loadingClassmate;
    public Observable<Response<Announce[]  , CourseData>> loadingAnnounces;

    public String courseid;
    public String id;
    public String name;
    public String teacher;
    public int notice;
    public int homework;
    public int exam;
    public boolean warning;

    private Ecourse ecourse;

    public Course(Ecourse content) {
        this.setEcourse(content);
    }

    private void syncAnnounceContent(final Announce[] announces) {
        new Thread(() -> {
            if (announces != null)
                for (Announce announce : announces)
                    announce.getContent();
        }).start();
    }

    public Observable<Response<Announce[], CourseData>> getAnnounces() {
        if (loadingAnnounces == null)
            loadingAnnounces = ecourse.fetch(AnnounceSource.request(this))
                    .doOnNext(response -> {
                        if (ecourse.getOfflineMode().compareTo(OfflineMode.VIEWED) <= 0)
                            syncAnnounceContent(response.data());
                    })
                    .compose(cache())
                    .doOnError(throwable -> loadingAnnounces = null);

        return loadingAnnounces;
    }

    public Observable<Response<Classmate[], CourseData>> getClassmate() {
        if (loadingClassmate == null)
            loadingClassmate = ecourse.fetch(ClassmateSource.request(this))
                    .compose(cache())
                    .doOnError(throwable -> loadingClassmate = null);


        return loadingClassmate;
    }

    public Observable<Response<FileGroup[], CourseData>> getFiles() {
        if (loadingFiles == null)
            loadingFiles = ecourse.fetch(FileGroupSource.request(this))
                    .compose(cache())
                    .doOnError(throwable -> loadingFiles = null);

        return loadingFiles;
    }

    public Observable<Response<Homework[], CourseData>> getHomework() {
        if (loadingHomework == null)
            loadingHomework = ecourse.fetch(HomeworkSource.request(this))
                    .compose(cache())
                    .doOnError(throwable -> loadingHomework = null);

        return loadingHomework;
    }

    public Observable<Response<RollCall, CourseData>> getRollCall() {
        if (loadingRollCall == null)
            loadingRollCall = ecourse.fetch(RollCallSource.request(this))
                    .compose(cache())
                    .doOnError(throwable -> loadingRollCall = null);

        return loadingRollCall;
    }

    public Observable<Response<ScoreGroup[], CourseData>> getScore() {
        if (loadingScore == null) {
            loadingScore = ecourse.fetch(ScoreSource.request(this))
                    .compose(cache())
                    .doOnError(throwable -> loadingScore = null);
        }

        return loadingScore;
    }

    public Ecourse getEcourse() {
        return ecourse;
    }

    public void setEcourse(Ecourse ecourse) {
        this.ecourse = ecourse;
    }

}
