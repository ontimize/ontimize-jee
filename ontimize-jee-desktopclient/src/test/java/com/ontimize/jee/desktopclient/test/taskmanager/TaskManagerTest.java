package com.ontimize.jee.desktopclient.test.taskmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.desktopclient.components.taskmanager.DefaultTask;
import com.ontimize.jee.desktopclient.components.taskmanager.TaskManagerGUI;
import com.ontimize.jee.desktopclient.components.taskmanager.TaskStatus;

public class TaskManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskManagerTest.class);

    public TaskManagerTest() {
    }

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final DefaultTask taskInfo = new DefaultTask("Mi tarea");
        TaskManagerGUI.getInstance().addTask(taskInfo);

        Runnable myTask = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException error) {
                        TaskManagerTest.logger.error(null, error);
                    }
                    taskInfo.updateProgress(i / 100.0, TaskStatus.RUNNING);
                }
                taskInfo.updateProgress(1, TaskStatus.COMPLETED);
            }
        };

        executor.execute(myTask);

    }

}
