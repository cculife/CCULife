package org.zankio.ccudata.kiki;

import android.test.InstrumentationTestCase;

import org.junit.Test;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.kiki.model.SemesterData;
import org.zankio.ccudata.kiki.model.TimeTable;
import org.zankio.ccudata.kiki.source.local.DatabaseTimeTableSource;
import org.zankio.ccudata.kiki.source.remote.Authenticate;
import org.zankio.ccudata.kiki.source.remote.TimetableSource;

import java.util.List;

public class KikiTest extends InstrumentationTestCase {
    private static final String username = "400000000";
    private static final String password = "1234567";

    @Test
    public void testFetch() throws Exception {
        Kiki kiki = new Kiki(getInstrumentation().getContext());
        kiki.user()
                .username(username)
                .password(password);

        Boolean data = kiki.fetch(Authenticate.request(username, password)).toBlocking().first().data();
        List<Response<TimeTable, SemesterData>> list = kiki.fetch(TimetableSource.request(104, 2)).toList().toBlocking().single();

        TimeTable timeTable = new DatabaseTimeTableSource(kiki).fetch(TimetableSource.request(104, 2));

    }
}