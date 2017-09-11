package org.zankio.ccudata.base;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runner.RunWith;
import org.zankio.ccudata.base.model.Request;
import org.zankio.ccudata.base.model.Response;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.base.source.SourceProperty;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

//import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class RepositoryTest {
    private static final String EXAMPLE_TYPE = "EXAMPLE";

    class ExampleSource<T> extends BaseSource<Void, T> {
        private String type;
        private int delay;
        private T result;
        private SourceProperty.Level order;
        private Exception exception;
        final Object o = new Object();

        public ExampleSource(String type, T result, SourceProperty.Level order) {
            this(type, result, 0, order);
        }

        public ExampleSource(String type, T result, int delay) {
            this(type, result, delay, SourceProperty.Level.LOW);
        }

        public ExampleSource(String type, T result) {
            this(type, result, 0, SourceProperty.Level.LOW);
        }

        public ExampleSource(String type, T result, int delay, SourceProperty.Level order) {
            this.type = type;
            this.delay = delay;
            this.result = result;
            this.order = order;
        }

        public ExampleSource<T> error(Exception exception)  {
            this.exception = exception;
            return this;
        }

        @Override
        public SourceProperty.Level getOrder() {
            return order;
        }

        @Override
        public T fetch(Request<T, Void> request) throws Exception {
            delay();
            if (exception != null) throw exception;
            return result;
        }

        public void delay() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String[] getDataType() {
            return new String[]{ type };
        }

        @Override
        public String toString() {
            return "Source \n" +
                    "delay: " + delay + ", \n" +
                    "type: " + type + ", \n" +
                    "result: " + result + ", \n" +
                    "level: " + order.name() + ", \n" +
                    "error: " + exception;
        }
    }

    @Test
    public void testFetch() throws Throwable {
        List result = testFetchInter(
                EXAMPLE_TYPE,
                new BaseSource[]{
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result"
                        )
                });

        Assert.assertArrayEquals(new String[] { "Result" }, result.toArray(new String[result.size()]));
        try {
            Assert.assertArrayEquals(new String[] { "Result!" }, result.toArray(new String[result.size()]));
            Assert.fail();
        } catch (ArrayComparisonFailure ignored) { ignored.printStackTrace();}
    }

    @Test
    public void testFetchError() throws Throwable {
        Exception exception = null;
        try {
            testFetchInter(
                    EXAMPLE_TYPE,
                    new BaseSource[]{
                            new ExampleSource<>(
                                    EXAMPLE_TYPE,
                                    "Result"
                            ).error(new Exception("error")),
                    });
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception == null)
                Assert.fail();
        }
    }

    @Test
    public void testFetchMulti() throws Throwable {
        List result = testFetchInter(
                EXAMPLE_TYPE,
                new BaseSource[]{
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result"
                        ),
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result1"
                        )
                });

        Collections.sort(result);
        Assert.assertArrayEquals(new String[] { "Result", "Result1" }, result.toArray(new String[result.size()]));
    }

    @Test
    public void testFetchMultiOrderSeq() throws Throwable {
        List result = testFetchInter(
                EXAMPLE_TYPE,
                new BaseSource[]{
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result"
                        ),
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result1",
                                500,
                                SourceProperty.Level.HIGH
                        )
                });

        Collections.sort(result);
        Assert.assertArrayEquals(new String[] {"Result", "Result1" }, result.toArray(new String[result.size()]));
    }

    @Test
    public void testFetchMultiOrderSingle() throws Throwable {
        List result = testFetchInter(
                EXAMPLE_TYPE,
                new BaseSource[]{
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result",
                                500,
                                SourceProperty.Level.LOW
                        ),
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result1",
                                SourceProperty.Level.HIGH
                        )
                });

        Collections.sort(result);
        Assert.assertArrayEquals(new String[] {"Result1"}, result.toArray(new String[result.size()]));
    }

    @Test
    public void testFetchSingleError() throws Throwable {
        List result = testFetchInter(
                EXAMPLE_TYPE,
                new BaseSource[]{
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result",
                                SourceProperty.Level.HIGH
                        ).error(new Exception("test error")),
                        new ExampleSource<>(
                                EXAMPLE_TYPE,
                                "Result1"
                        )
                });

        Collections.sort(result);
        Assert.assertArrayEquals(new String[] { "Result1" }, result.toArray(new String[result.size()]));
    }

    @Test
    public void testFetchAllError() throws Throwable {
        Exception exception = null;
        try {
            testFetchInter(
                    EXAMPLE_TYPE,
                    new BaseSource[]{
                            new ExampleSource<>(
                                    EXAMPLE_TYPE,
                                    "Result",
                                    SourceProperty.Level.LOW
                            ).error(new Exception("display error")),
                            new ExampleSource<>(
                                    EXAMPLE_TYPE,
                                    "Result1",
                                    500
                            ).error(new Exception("test error")),
                    });
        } catch (Exception e) {
            exception = e;
        } finally {
            if (exception == null)
                Assert.fail();
            else if (!"display error".equals(exception.getCause().getMessage()))
                Assert.fail("not correct exception: " + exception.getCause());
        }
    }

    public List testFetchInter(String type, BaseSource[] sources) throws Throwable {
        final Throwable[] error = new Throwable[] { null };

        Repository repository = new Repository(null) {
            @Override
            protected BaseSource[] getSources() {
                return sources;
            }
        };
        Date start = new Date();
        List result = repository
                .fetch(new Request<>(type, "", String.class))
                .doOnError(throwable -> {
                    System.out.println("error");
                    error[0] = throwable;
                })
                .map(Response::data)
                .doOnNext(s -> System.out.println("output : " + s))
                .toList()
                .toBlocking()
                .single();
        Date end = new Date();
        long diff = end.getTime() - start.getTime();//as given

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        Log.d("TEST", "TIME: " + diff);


        if (error[0] != null) {
            throw error[0];
        }
        return result;
    }

}