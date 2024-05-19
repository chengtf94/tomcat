package org.apache.tomcat.util.threads;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * LimitLatch：连接控制器，负责控制最⼤连接数，NIO模式下默认是10000，达到这个阈值后，连接请求被拒绝。
 * 当连接数到达最⼤时阻塞线程，直到后续组件处理完⼀个连接后将连接数减1。注意：到达最⼤连接数后操作系统底层还是会接收客户端连接，但⽤户层已经不再接收。
 */
public class LimitLatch {
    private static final Log log = LogFactory.getLog(LimitLatch.class);

    private final Sync sync;
    private final AtomicLong count;
    private volatile long limit;
    private volatile boolean released = false;

    /**
     * 构造方法
     */
    public LimitLatch(long limit) {
        this.limit = limit;
        this.count = new AtomicLong(0);
        this.sync = new Sync();
    }

    /**
     * 线程调⽤这个⽅法来获得接收新连接的许可，线程可能被阻塞（如果暂时⽆法获取，这个线程会被阻塞到AQS的队列中）
     */
    public void countUpOrAwait() throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Counting up["+Thread.currentThread().getName()+"] latch="+getCount());
        }
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 调⽤这个⽅法来释放⼀个连接许可，那么前⾯阻塞的线程可能被唤醒
     */
    public long countDown() {
        sync.releaseShared(0);
        long result = getCount();
        if (log.isDebugEnabled()) {
            log.debug("Counting down["+Thread.currentThread().getName()+"] latch="+result);
        }
        return result;
    }

    private class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 1L;

        Sync() {
        }

        @Override
        protected int tryAcquireShared(int ignored) {
            long newCount = count.incrementAndGet();
            if (!released && newCount > limit) {
                // Limit exceeded
                count.decrementAndGet();
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            count.decrementAndGet();
            return true;
        }
    }

    public boolean releaseAll() {
        released = true;
        return sync.releaseShared(0);
    }

    public void reset() {
        this.count.set(0);
        released = false;
    }

    public boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    public Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    public long getCount() {
        return count.get();
    }
    public long getLimit() {
        return limit;
    }
    public void setLimit(long limit) {
        this.limit = limit;
    }

}
