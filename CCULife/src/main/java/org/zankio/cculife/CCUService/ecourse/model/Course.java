package org.zankio.cculife.CCUService.ecourse.model;

import android.os.AsyncTask;

import org.zankio.cculife.CCUService.base.constant.OfflineMode;
import org.zankio.cculife.CCUService.base.listener.IOnUpdateListener;
import org.zankio.cculife.CCUService.base.listener.OnUpdateListener;
import org.zankio.cculife.CCUService.base.source.BaseSource;
import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.source.remote.AnnounceSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.ClassmateSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.FileSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.HomeworkSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.RollCallSource;
import org.zankio.cculife.CCUService.ecourse.source.remote.ScoreSource;

import java.util.HashMap;
import java.util.HashSet;

public class Course {
    private final static int TASK_ANNOUNCE = 0;
    //private final static int TASK_ANNOUNCE_CONTENT = 1;
    private final static int TASK_CLASSMATE = 2;
    private final static int TASK_FILE = 3;
    private final static int TASK_SCORE = 4;
    private final static int TASK_HOMEWORK = 5;
    private final static int TASK_ROLLCALL = 6;
    private HashMap<Integer, FetchTask> loading = new HashMap<>();

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

    public boolean getAnnounces(IOnUpdateListener<Announce[]> listener) {
        if (this.announces != null) {
            listener.onNext(AnnounceSource.TYPE, this.announces, null);
            return false;
        }

        FetchTask fetchTask = loading.get(TASK_ANNOUNCE);
        if (fetchTask != null) {
            fetchTask.add(listener);
            return true;
        }

        fetchTask = new FetchTask(TASK_ANNOUNCE);
        loading.put(TASK_ANNOUNCE, fetchTask);
        fetchTask.add(new OnUpdateListener<Announce[]>(listener) {
            @Override
            public void onNext(String type, Announce[] data, BaseSource source) {
                if (ecourse.getOfflineMode().compareTo(OfflineMode.VIEWED) >= 0)
                    syncAnnounceContent(data);

                Course.this.announces = data;
                super.onNext(type, data, source);
            }
        });
        fetchTask.add(ecourse.fetch(AnnounceSource.TYPE, fetchTask, this));
        return true;
    }

    private void syncAnnounceContent(final Announce[] announces) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (announces != null)
                    for (Announce announce : announces)
                        announce.getContent();
            }
        }).start();
    }

    public boolean getClassmate(IOnUpdateListener<Classmate[]> listener) {
        if (this.classmate != null) {
            listener.onNext(ClassmateSource.TYPE, this.classmate, null);
            return false;
        }

        FetchTask fetchTask = loading.get(TASK_CLASSMATE);
        if (fetchTask != null) {
            fetchTask.add(listener);
            return true;
        }

        fetchTask = new FetchTask(TASK_CLASSMATE);
        loading.put(TASK_CLASSMATE, fetchTask);
        fetchTask.add(new OnUpdateListener<Classmate[]>(listener) {
            @Override
            public void onNext(String type, Classmate[] data, BaseSource source) {
                Course.this.classmate = data;
                super.onNext(type, data, source);
            }
        });
        fetchTask.add(ecourse.fetch(ClassmateSource.TYPE, fetchTask, this));
        return true;
    }

    public boolean getFiles(IOnUpdateListener<FileGroup[]> listener) {
        if (this.files != null) {
            listener.onNext(FileSource.TYPE, this.files, null);
            return false;
        }

        FetchTask fetchTask = loading.get(TASK_FILE);
        if (fetchTask != null) {
            fetchTask.add(listener);
            return true;
        }

        fetchTask = new FetchTask(TASK_FILE);
        loading.put(TASK_FILE, fetchTask);
        fetchTask.add(new OnUpdateListener<FileGroup[]>(listener) {
            @Override
            public void onNext(String type, FileGroup[] data, BaseSource source) {
                Course.this.files = data;
                super.onNext(type, data, source);
            }
        });
        fetchTask.add(ecourse.fetch(FileSource.TYPE, fetchTask, this));
        return true;
    }

    public boolean getHomework(IOnUpdateListener<Homework[]> listener) {
        if (this.homeworks != null) {
            listener.onNext(HomeworkSource.TYPE, this.homeworks, null);
            return false;
        }

        FetchTask fetchTask = loading.get(TASK_HOMEWORK);
        if (fetchTask != null) {
            fetchTask.add(listener);
            return true;
        }

        fetchTask = new FetchTask(TASK_HOMEWORK);
        loading.put(TASK_HOMEWORK, fetchTask);
        fetchTask.add(new OnUpdateListener<Homework[]>(listener) {
            @Override
            public void onNext(String type, Homework[] data, BaseSource source) {
                Course.this.homeworks = data;
                super.onNext(type, data, source);
            }
        });
        fetchTask.add(ecourse.fetch(HomeworkSource.TYPE, fetchTask, this));
        return true;
    }

    public boolean getRollCall(IOnUpdateListener<RollCall[]> listener) {
        if (this.rollcalls != null) {
            listener.onNext(RollCallSource.TYPE, this.rollcalls, null);
            return false;
        }

        FetchTask fetchTask = loading.get(TASK_ROLLCALL);
        if (fetchTask != null) {
            fetchTask.add(listener);
            return true;
        }

        fetchTask = new FetchTask(TASK_ROLLCALL);
        loading.put(TASK_ROLLCALL, fetchTask);
        fetchTask.add(new OnUpdateListener<RollCall[]>(listener) {
            @Override
            public void onNext(String type, RollCall[] data, BaseSource source) {
                Course.this.rollcalls = data;
                super.onNext(type, data, source);
            }
        });
        fetchTask.add(ecourse.fetch(RollCallSource.TYPE, fetchTask, this));
        return true;
    }

    public boolean getScore(IOnUpdateListener<ScoreGroup[]> listener) {
        if (this.scores != null) {
            listener.onNext(ScoreSource.TYPE, this.scores, null);
            return false;
        }

        FetchTask fetchTask = loading.get(TASK_SCORE);
        if (fetchTask != null) {
            fetchTask.add(listener);
            return true;
        }

        fetchTask = new FetchTask(TASK_SCORE);
        fetchTask.add(new OnUpdateListener<ScoreGroup[]>(listener) {
            @Override
            public void onNext(String type, ScoreGroup[] data, BaseSource source) {
                Course.this.scores = data;
                super.onNext(type, data, source);
            }
        });
        fetchTask.add(ecourse.fetch(ScoreSource.TYPE, fetchTask, this));
        return true;
    }

    public Ecourse getEcourse() {
        return ecourse;
    }

    public void setEcourse(Ecourse ecourse) {
        this.ecourse = ecourse;
    }

    private class FetchTask implements IOnUpdateListener<Object>{
        private int taskId;
        public AsyncTask[] tasks;
        public HashSet<IOnUpdateListener> listeners;

        public FetchTask(int taskId) {
            this.taskId = taskId;
            this.listeners = new HashSet<>();
        }
        public FetchTask add(IOnUpdateListener listener) {
            listeners.add(listener);
            return this;
        }
        public FetchTask add(AsyncTask[] tasks) {
            loading.put(taskId, this);
            this.tasks = tasks;
            return this;
        }

        @Override
        public void onComplete(String type) {
            for (IOnUpdateListener listener: listeners) {
                listener.onComplete(type);
            }
            loading.remove(taskId);
        }

        @Override
        public void onError(String type, Exception err, BaseSource source) {
            for (IOnUpdateListener listener: listeners) {
                listener.onError(type, err, source);
            }

        }

        @Override
        public void onNext(String type, Object data, BaseSource source) {
            for (IOnUpdateListener listener: listeners) {
                listener.onNext(type, data, source);
            }

        }
    }
}
