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
    private final static int TASK_ANNOUNCE = 0;
    //private final static int TASK_ANNOUNCE_CONTENT = 1;
    private final static int TASK_CLASSMATE = 2;
    private final static int TASK_FILE = 3;
    private final static int TASK_SCORE = 4;
    private final static int TASK_HOMEWORK = 5;
    private final static int TASK_ROLLCALL = 6;
    //private HashMap<Integer, SourceExecutor> loading = new HashMap<>();

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
        if (this.files != null) {
            return Observable.just(new Response<>(this.announces, null));
        }

        return ecourse.fetch(AnnounceSource.request(this))
                .doOnNext(response -> {
                    if (ecourse.getOfflineMode().compareTo(OfflineMode.VIEWED) >= 0)
                        syncAnnounceContent(response.data());

                    this.announces = response.data();
                });
    }

    public Observable<Response<Classmate[], CourseData>> getClassmate() {
        if (this.files != null) {
            return Observable.just(new Response<>(this.classmate, null));
        }

        return ecourse.fetch(ClassmateSource.request(this))
                .doOnNext(response -> this.classmate = response.data());
    }

    public Observable<Response<FileGroup[], CourseData>> getFiles() {
        if (this.files != null) {
            return Observable.just(new Response<>(this.files, null));
        }

        return ecourse.fetch(FileGroupSource.request(this))
                .doOnNext(response -> this.files = response.data());
    }

    public Observable<Response<Homework[], CourseData>> getHomework() {
        if (this.rollcalls != null) {
            return Observable.just(new Response<>(this.homeworks, null));
        }

        return ecourse.fetch(HomeworkSource.request(this))
                .doOnNext(response -> this.homeworks = response.data());
    }

    public Observable<Response<RollCall[], CourseData>> getRollCall() {
        if (this.rollcalls != null) {
            return Observable.just(new Response<>(this.rollcalls, null));
        }

        return ecourse.fetch(RollCallSource.request(this))
                .doOnNext(response -> this.rollcalls = response.data());
    }

    // TODO: 2016/7/20 double loading
    public rx.Observable<Response<ScoreGroup[], CourseData>> getScore() {
        if (this.scores != null) {
            return Observable.just(new Response<>(this.scores, null));
        }

        return ecourse.fetch(ScoreSource.request(this))
            .doOnNext(response -> this.scores = response.data());
    }

    public Ecourse getEcourse() {
        return ecourse;
    }

    public void setEcourse(Ecourse ecourse) {
        this.ecourse = ecourse;
    }

}
