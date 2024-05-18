package org.apache.tomcat.util.threads;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * 定制任务线程
 *
 */
public class TaskThread extends Thread {
    private static final Log log = LogFactory.getLog(TaskThread.class);

    /** 创建时间戳 */
    private final long creationTime;
    public final long getCreationTime() {
        return creationTime;
    }

    /** 构造方法 */
    public TaskThread(ThreadGroup group, Runnable target, String name) {
        super(group, new WrappingRunnable(target), name);
        this.creationTime = System.currentTimeMillis();
    }
    public TaskThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, new WrappingRunnable(target), name, stackSize);
        this.creationTime = System.currentTimeMillis();
    }

    private static class WrappingRunnable implements Runnable {
        private Runnable wrappedRunnable;
        WrappingRunnable(Runnable wrappedRunnable) {
            this.wrappedRunnable = wrappedRunnable;
        }
        @Override
        public void run() {
            try {
                wrappedRunnable.run();
            } catch(StopPooledThreadException exc) {
                log.debug("Thread exiting on purpose", exc);
            }
        }
    }

}
