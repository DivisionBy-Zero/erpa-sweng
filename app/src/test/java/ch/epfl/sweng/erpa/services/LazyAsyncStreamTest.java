package ch.epfl.sweng.erpa.services;

import org.junit.Test;

import java.util.Iterator;

import ch.epfl.sweng.erpa.services.LazyAsyncStream;
import ch.epfl.sweng.erpa.util.TestUtils;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class LazyAsyncStreamTest {
    @Test
    public void testAtLeastNElements() {
        int count = 0;
        LazyAsyncStream<Object> stream = new SyntheticLazyAsyncStream();
        stream.asyncTaskService = new TestUtils.SynchronousTaskService();
        int maxCount = 10000;
        Iterator<Object> it = stream.iterator();
        while (count < maxCount && it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(maxCount, count);
    }

    @Test(expected = AssertionError.class)
    public void testBadConstructor() {
        new SyntheticLazyAsyncStream(true);
    }

    @Test
    public void testAllElements() {
        int maxCount = 500;
        LazyAsyncStream<Object> stream = new SyntheticLazyAsyncStream(maxCount);
        stream.asyncTaskService = new TestUtils.SynchronousTaskService();
        //noinspection StatementWithEmptyBody
        for (Object o : stream);
        assertEquals(stream.size(), maxCount);
        assertEquals(stream.getAsList().size(), maxCount);
    }

    @Test
    public void testUpdateObservers() {
        LazyAsyncStream<Object> stream = new SyntheticLazyAsyncStream(5);
        stream.asyncTaskService = new TestUtils.SynchronousTaskService();
        TestUtils.Fuse f = new TestUtils.Fuse();
        stream.addObserver(o -> f.ignite());
        stream.updateObservers();
        assertTrue(f.ignited);
    }

    @Test
    public void testRefresh() {
        int maxCount = 500;
        LazyAsyncStream<Object> stream = new SyntheticLazyAsyncStream(maxCount);
        stream.asyncTaskService = new TestUtils.SynchronousTaskService();
        //noinspection StatementWithEmptyBody
        for (Object o : stream);
        assertNotNull(stream.get(0));
        assertEquals(stream.size(), maxCount);
        assertEquals(stream.getAsList().size(), maxCount);
        stream.refreshDataAndReset();
        assertEquals(stream.size(), stream.chunks);
    }

    private class SyntheticLazyAsyncStream extends LazyAsyncStream<Object> {
        private final int max;

        public SyntheticLazyAsyncStream() {
            this.max = Integer.MAX_VALUE;
        }

        public SyntheticLazyAsyncStream(int max) {
            super();
            this.max = max;
        }

        public SyntheticLazyAsyncStream(boolean b) {
            super(-5);
            this.max = 0;
        }

        @Override protected void loadAhead(int from) {
            for (int i = 0; i < chunks && elements.size() < max; i++) {
                elements.add(new Object());
            }
        }
    }
}
