package org.zankio.ccudata.ecourse;

import android.test.InstrumentationTestCase;
import android.util.Log;

import org.junit.Test;
import org.zankio.ccudata.ecourse.model.Announce;
import org.zankio.ccudata.ecourse.model.Course;
import org.zankio.ccudata.ecourse.model.FileGroup;
import org.zankio.ccudata.ecourse.model.Homework;
import org.zankio.ccudata.ecourse.model.RollCall;
import org.zankio.ccudata.ecourse.model.ScoreGroup;
import org.zankio.ccudata.ecourse.source.local.DatabaseAnnounceSource;
import org.zankio.ccudata.ecourse.source.local.DatabaseCourseListSource;
import org.zankio.ccudata.ecourse.source.local.DatabaseScoreSource;
import org.zankio.ccudata.ecourse.source.remote.AnnounceSource;
import org.zankio.ccudata.ecourse.source.remote.Authenticate;
import org.zankio.ccudata.ecourse.source.remote.CourseListSource;
import org.zankio.ccudata.ecourse.source.remote.ScoreSource;

import java.util.Arrays;

public class EcourseTest extends InstrumentationTestCase {
    private static final String username = "401234567";
    private static final String password = "qwertyuio";

    @Test
    public void testFetch() throws Exception {
        Ecourse ecourse = new Ecourse(getInstrumentation().getContext());
        ecourse.user()
               .username(username)
               .password(password);

        Boolean auth = ecourse.fetch(Authenticate.request(username, password)).toBlocking().last().data();

        Log.d("TEST", "AUTH: " + auth);
        Course[] courses = ecourse.fetch(CourseListSource.request()).toBlocking().last().data();
        Course[] coursesDB = new DatabaseCourseListSource(ecourse).fetch(CourseListSource.request());

        Log.d("TEST", "Course List: " + Arrays.toString(courses));
        Announce[] announces   =     courses[2].getAnnounces().toBlocking().last().data();
        Announce[] announcesDB = new DatabaseAnnounceSource(ecourse).fetch(AnnounceSource.request(courses[2]));
        FileGroup[] fileGroups =     courses[2].getFiles()    .toBlocking().last().data();
        Homework[] homeworks   =     courses[2].getHomework() .toBlocking().last().data();
        RollCall rollCalls   =     courses[2].getRollCall() .toBlocking().last().data();
        ScoreGroup[] scoreGroups =   courses[2].getScore()    .toBlocking().last().data();
        ScoreGroup[] scoreGroupsDB = new DatabaseScoreSource(ecourse).fetch(ScoreSource.request(courses[2]));
    }
}
