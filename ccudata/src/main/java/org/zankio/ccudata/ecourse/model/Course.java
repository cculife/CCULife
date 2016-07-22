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


public class Course {
    public Observable<Response<ScoreGroup[], CourseData>> loadingScore;
    public Observable<Response<RollCall[]  , CourseData>> loadingRollCall;
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

    private Announce[] announces;
    private Classmate[] classmate;
    private FileGroup[] files;
    private Homework[] homeworks;
    private RollCall[] rollcalls;
    private ScoreGroup[] scores;
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
        Observable<Response<Announce[], CourseData>> cache;
        if (this.files != null) {
            return Observable.just(new Response<>(this.announces, null));
        }

        if (loadingAnnounces != null)
            return loadingAnnounces;

        cache = ecourse.fetch(AnnounceSource.request(this))
                .doOnNext(response -> {
                    if (ecourse.getOfflineMode().compareTo(OfflineMode.VIEWED) >= 0)
                        syncAnnounceContent(response.data());

                    this.announces = response.data();
                })
                .doOnTerminate(() -> loadingAnnounces = null)
                .cache();

        loadingAnnounces = cache;
        return cache;
    }

    public Observable<Response<Classmate[], CourseData>> getClassmate() {
        Observable<Response<Classmate[], CourseData>> cache;
        if (this.files != null) {
            return Observable.just(new Response<>(this.classmate, null));
        }

        if (loadingClassmate != null)
            return loadingClassmate;

        cache = ecourse.fetch(ClassmateSource.request(this))
                .doOnNext(response -> this.classmate = response.data())
                .doOnTerminate(() -> loadingClassmate = null)
                .cache();

        loadingClassmate = cache;
        return cache;
    }

    public Observable<Response<FileGroup[], CourseData>> getFiles() {
        Observable<Response<FileGroup[], CourseData>> cache;
        if (this.files != null) {
            return Observable.just(new Response<>(this.files, null));
        }

        if (loadingFiles != null) {
            return loadingFiles;
        }

        cache = ecourse.fetch(FileGroupSource.request(this))
                .doOnNext(response -> this.files = response.data())
                .doOnTerminate(() -> loadingFiles = null)
                .cache();

        loadingFiles = cache;
        return cache;
    }

    public Observable<Response<Homework[], CourseData>> getHomework() {
        Observable<Response<Homework[], CourseData>> cache;
        if (this.rollcalls != null) {
            return Observable.just(new Response<>(this.homeworks, null));
        }

        if (loadingHomework != null)
            return loadingHomework;

        cache = ecourse.fetch(HomeworkSource.request(this))
                .doOnNext(response -> this.homeworks = response.data())
                .doOnTerminate(() -> loadingHomework = null)
                .cache();

        loadingHomework = cache;
        return cache;
    }

    public Observable<Response<RollCall[], CourseData>> getRollCall() {
        Observable<Response<RollCall[], CourseData>> cache;
        if (this.rollcalls != null) {
            return Observable.just(new Response<>(this.rollcalls, null));
        }

        if (loadingRollCall != null)
            return loadingRollCall;

        cache = ecourse.fetch(RollCallSource.request(this))
                .doOnNext(response -> this.rollcalls = response.data())
                .doOnTerminate(() -> loadingRollCall = null)
                .cache();

        loadingRollCall = cache;
        return cache;
    }

    public Observable<Response<ScoreGroup[], CourseData>> getScore() {
        Observable<Response<ScoreGroup[], CourseData>> cache;
        if (this.scores != null) {
            return Observable.just(new Response<>(this.scores, null));
        }

        if (loadingScore != null) {
            return loadingScore;
        }

        cache = ecourse.fetch(ScoreSource.request(this))
                .doOnNext(response -> this.scores = response.data())
                .doOnTerminate(() -> loadingScore = null)
                .cache();

        loadingScore = cache;
        return cache;

    }

    public Ecourse getEcourse() {
        return ecourse;
    }

    public void setEcourse(Ecourse ecourse) {
        this.ecourse = ecourse;
    }

}
