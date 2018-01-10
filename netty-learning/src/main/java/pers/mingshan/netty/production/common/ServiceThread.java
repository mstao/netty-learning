package pers.mingshan.netty.production.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServiceThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ServiceThread.class);

    private static final long JOIN_TIME = 90 * 1000;
    
    protected final Thread thread;
    protected volatile boolean hasNotified = false;
    protected volatile boolean stoped = false;

    public ServiceThread() {
        this.thread = new Thread(this, this.getServiceName());
    }

    protected abstract String getServiceName();

    public void start() {
        this.thread.start();
    }

    public void stop() {
        this.stop(false);
    }
    
    
    public void stop(final boolean interrupt) {
        this.stoped = true;
        logger.info("stop thread " + this.getServiceName()  + " interrupt " + interrupt);

        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        if (interrupt) {
            this.thread.interrupt();
        }
    }

    public void makeStop() {
        this.stoped = true;
        logger.info("makestop thread " + this.getServiceName());
    }

    public void shutdown(final boolean interrupt) {
        this.stoped = true;
        logger.info("shutdown thread " + this.getServiceName() + " interrupt " + interrupt);

        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }

        try {
            if (interrupt) {
                this.thread.interrupt();
            }

            long beginTime = System.currentTimeMillis();
            this.thread.join(this.getJointime());
            long eclipseTime = System.currentTimeMillis() - beginTime;
            logger.info("join thread " + this.getServiceName() + " eclipse time(ms) " + eclipseTime + " "
                    + this.getJointime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void wakeup() {
        synchronized (this) {
            if (!this.hasNotified) {
                this.hasNotified = true;
                this.notify();
            }
        }
    }

    protected void waitForRunning(long interval) {
        synchronized (this) {
            if (this.hasNotified) {
                this.hasNotified = false;
                this.onWaitEnd();
                return;
            }

            try {
                this.wait(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.hasNotified = false;
                this.onWaitEnd();
            }
        }
    }

    protected void onWaitEnd() {
    }

    public boolean isStoped() {
        return stoped;
    }

    public long getJointime() {
        return JOIN_TIME;
    }

}
