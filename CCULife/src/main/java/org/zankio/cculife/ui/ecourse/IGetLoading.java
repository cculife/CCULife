package org.zankio.cculife.ui.ecourse;

public interface IGetLoading {
    boolean isLoading();
    void setLoadedListener(CourseFragment.LoadingListener listener);
}
