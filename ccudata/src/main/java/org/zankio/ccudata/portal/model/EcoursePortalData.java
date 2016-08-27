package org.zankio.ccudata.portal.model;

import org.zankio.ccudata.ecourse.constant.Urls;

import rx.Observable;

public class EcoursePortalData extends PortalData{
    private String courseID;

    public EcoursePortalData() {
        super(
                "http://ecourse.ccu.edu.tw/php/getssoCcuRight.php",
                "0000"
        );
    }

    public EcoursePortalData setCourseID(String courseID) {
        this.courseID = courseID;
        return this;
    }

    @Override
    public Observable.Transformer<String, String> after() {
        return stringObservable -> {
            if (courseID != null) {
                return stringObservable.concatWith(
                        Observable.just(String.format(Urls.COURSE_SELECT, courseID))
                );
            }

            return stringObservable;
        };
    }
}
