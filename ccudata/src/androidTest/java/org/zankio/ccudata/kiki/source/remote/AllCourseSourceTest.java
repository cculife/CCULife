package org.zankio.ccudata.kiki.source.remote;

import android.util.Log;

import org.junit.Test;
import org.zankio.ccudata.base.model.Request;

public class AllCourseSourceTest {

    @Test
    public void testFetch() throws Exception {
        Log.e("test", new AllCourseSource().fetch(new Request<>()));

    }
}