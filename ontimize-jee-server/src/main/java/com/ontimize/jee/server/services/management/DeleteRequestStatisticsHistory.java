package com.ontimize.jee.server.services.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;

public class DeleteRequestStatisticsHistory extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(DeleteRequestStatisticsHistory.class);

    private final Object semaphore = new Object();

    private volatile boolean isRunning = false;

    @Autowired
    private IServerManagementService serverManagementService;

    private final long millisWait;

    private final int days;

    public DeleteRequestStatisticsHistory() {
        this(3600000L, 10);
    }

    public DeleteRequestStatisticsHistory(Long millis, int days) {
        this.setName("DeleteRequestStatisticsThread");
        this.millisWait = millis;
        this.days = days;
    }

    public void kill() {
        this.setRunning(false);
    }

    @Override
    public void run() {
        super.run();
        this.setRunning(true);
        while (true) {
            if (!DeleteRequestStatisticsHistory.this.isRunning()) {
                try {
                    DeleteRequestStatisticsHistory.logger.info("STOPPED!...");
                    synchronized (DeleteRequestStatisticsHistory.this.semaphore) {
                        DeleteRequestStatisticsHistory.this.semaphore.wait();
                    }
                } catch (InterruptedException e) {
                    DeleteRequestStatisticsHistory.logger.error(null, e);
                }
            }
            try {
                DeleteRequestStatisticsHistory.logger.info("Starting...");

                // Clean
                DeleteRequestStatisticsHistory.this.cleanRequestStatistics();
            } catch (Exception e) {
                DeleteRequestStatisticsHistory.logger.error("Uncatched error. Detail:", e);
            } finally {
                if (DeleteRequestStatisticsHistory.this.isRunning()) {
                    DeleteRequestStatisticsHistory.logger.info("Programing next execution...");
                    try {
                        new TimerThread(DeleteRequestStatisticsHistory.this.millisWait).start();
                        synchronized (DeleteRequestStatisticsHistory.this.semaphore) {
                            DeleteRequestStatisticsHistory.this.semaphore.wait();
                        }
                    } catch (Exception e) {
                        DeleteRequestStatisticsHistory.logger.error("Error: ", e);
                    }
                }
            }
        }
    }

    public void restart() {
        if (!this.isRunning()) {
            this.awake();
        }
    }

    protected void awake() {
        synchronized (this.semaphore) {
            this.semaphore.notify();
            this.setRunning(true);
        }
    }

    private void cleanRequestStatistics() {
        EntityResult deleteRequestStatistics = this.serverManagementService.deleteStatistics(this.days);
        if ((deleteRequestStatistics != null)
                && (deleteRequestStatistics.getCode() == EntityResult.OPERATION_SUCCESSFUL)) {
            DeleteRequestStatisticsHistory.logger.info("request statistics deleted.");
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    class TimerThread extends Thread {

        private final long millis;

        public TimerThread(long millis) {
            super("Delete Request Statistics");
            this.millis = millis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(this.millis);
                if (DeleteRequestStatisticsHistory.this.isRunning()) {
                    DeleteRequestStatisticsHistory.this.awake();
                }
            } catch (InterruptedException e) {
                DeleteRequestStatisticsHistory.logger.error(null, e);
            }

        }

    }

}
