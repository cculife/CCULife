package org.zankio.ccudata.ecourse.source.remote;

import org.junit.Assert;
import org.junit.Test;
import org.zankio.ccudata.ecourse.Ecourse;

public class AuthenticateTest {

    @Test
    public void testFetch() throws Exception {
        System.setProperty("jsse.enableSNIExtension", "false");

        try {
            new Authenticate()
                    .setContext(new Ecourse(null))
                    .fetch(Authenticate.request("40000000", "1234567"));
            Assert.fail();
        } catch (Exception e) {
            if (!"帳號或密碼錯誤".equals(e.getMessage()))
                Assert.fail();
        }
    }
}