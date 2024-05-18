package org.apache.tomcat.util.threads;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.util.res.StringManager;

/**
 * 定制版任务队列
 */
public class TaskQueue extends LinkedBlockingQueue<Runnable> {

    private static final long serialVersionUID = 1L;
    protected static final StringManager sm = StringManager.getManager(TaskQueue.class);

    /** 所属线程池 */
    private transient volatile ThreadPoolExecutor parent = null;
    public void setParent(ThreadPoolExecutor tp) {
        parent = tp;
    }

    /**
     * 构造方法
     */
    public TaskQueue() {
        super();
    }
    public TaskQueue(int capacity) {
        super(capacity);
    }
    public TaskQueue(Collection<? extends Runnable> c) {
        super(c);
    }

    /**
     * 若任务被Executor拒绝执行后，则添加该任务入队
     */
    public boolean force(Runnable o) {
        if (parent == null || parent.isShutdown()) {
            throw new RejectedExecutionException(sm.getString("taskQueue.notRunning"));
        }
        return super.offer(o); //forces the item onto the queue, to be used if the task is rejected
    }

    @Deprecated
    public boolean force(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
        if (parent == null || parent.isShutdown()) {
            throw new RejectedExecutionException(sm.getString("taskQueue.notRunning"));
        }
        return super.offer(o,timeout,unit); //forces the item onto the queue, to be used if the task is rejected
    }

    /**
     * 线程池调⽤任务队列的⽅法时，当前线程数肯定已经⼤于核⼼线程数了
     * 只有当前线程数⼤于核⼼线程数、⼩于最⼤线程数，并且已提交的任务个数⼤于当前线程数时，即线程不够⽤了，但是线程数⼜没达到极限，才会去创建新的线程。
     * 这就是为什么Tomcat需要维护已提交任务数这个变量，它的⽬的就是在任务队列的⻓度⽆限制的情况下，让线程池有机会创建新的线程。
     */
    @Override
    public boolean offer(Runnable o) {
      //we can't do any checks
        if (parent == null) {
            return super.offer(o);
        }
        // 如果线程数已经到了最⼤值，不能创建新线程了，只能把任务添加到任务队列
        if (parent.getPoolSizeNoLock() == parent.getMaximumPoolSize()) {
            return super.offer(o);
        }
        // 执⾏到这⾥，表明当前线程数⼤于核⼼线程数，并且⼩于最⼤线程数，表明是可以创建新线程的，那到底要不要创建呢？分两种情况：
        // 1. 如果已提交的任务数⼩于当前线程数，表示还有空闲线程，⽆需创建新线程
        if (parent.getSubmittedCount() <= parent.getPoolSizeNoLock()) {
            return super.offer(o);
        }
        // 2. 如果已提交的任务数⼤于当前线程数，线程不够⽤了，返回false去创建新线程
        if (parent.getPoolSizeNoLock() < parent.getMaximumPoolSize()) {
            return false;
        }
        // if we reached here, we need to add it to the queue
        return super.offer(o);
    }

    @Override
    public Runnable poll(long timeout, TimeUnit unit)
            throws InterruptedException {
        Runnable runnable = super.poll(timeout, unit);
        if (runnable == null && parent != null) {
            parent.stopCurrentThreadIfNeeded();
        }
        return runnable;
    }

    @Override
    public Runnable take() throws InterruptedException {
        if (parent != null && parent.currentThreadShouldBeStopped()) {
            return poll(parent.getKeepAliveTime(TimeUnit.MILLISECONDS),
                    TimeUnit.MILLISECONDS);
        }
        return super.take();
    }

}
