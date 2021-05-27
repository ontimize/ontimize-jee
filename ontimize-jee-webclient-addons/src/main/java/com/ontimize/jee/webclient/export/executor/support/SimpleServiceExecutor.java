/**
 * SimpleServiceExecutor.java 04-oct-2016
 *
 * Copyright 2016 INDITEX. Departamento de Sistemas
 */
package com.ontimize.jee.webclient.export.executor.support;


import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import com.ontimize.jee.webclient.export.executor.CallableTask;
import com.ontimize.jee.webclient.export.executor.ExecutionResult;
import com.ontimize.jee.webclient.export.executor.ExecutionStateControl;
import com.ontimize.jee.webclient.export.executor.ServiceExecutor;
import com.ontimize.jee.webclient.export.executor.TaskControl;
import com.ontimize.jee.webclient.export.executor.WorkerConfigurer;
import com.ontimize.jee.webclient.export.executor.WorkerListener;
import com.ontimize.jee.webclient.export.executor.callback.OnFailed;
import com.ontimize.jee.webclient.export.executor.callback.OnSucceeded;
import com.ontimize.jee.webclient.export.executor.statecontrol.SimpleExecutionStateControl;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * Implementacion de {@link ServiceExecutor} basica.
 *
 *
 */
public class SimpleServiceExecutor implements ServiceExecutor {

    /** The executor. */
    private Executor executor;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ExecutionResult<T> execute(final CallableTask<T> callable, final WorkerConfigurer<T> workerConfigurer,
            final ExecutionStateControl<T> executionStateControl, final WorkerListener... workerListeners) {

        final SimpleService<T> service = new SimpleService<>(callable, executionStateControl, workerListeners);
        if (workerConfigurer != null) {
            workerConfigurer.configure(service);
        }
        startListeners(service, workerListeners);
        if (this.executor != null) {
            service.setExecutor(this.executor);
        }
        service.start();
        return new ExecutionResult<>(service, service.getTask());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> ExecutionResult<T> execute(final CallableTask<T> callable, final OnSucceeded<T> onSucceeded,
            final OnFailed onFailed, final WorkerListener... workerListeners) {
        return execute(callable, null, new SimpleExecutionStateControl<>(onSucceeded, onFailed), workerListeners);
    }

    /**
     * Start listeners.
     * @param <T> tipo generico
     * @param worker worker
     * @param workerListeners worker listeners
     */
    @SuppressWarnings("unchecked")
    private static void startListeners(@SuppressWarnings("rawtypes") final Worker worker,
            final WorkerListener[] workerListeners) {
        if (workerListeners != null) {
            for (final WorkerListener workerListener : workerListeners) {
                workerListener.onStart(worker);
            }
        }

    }

    /**
     * Finish listeners.
     * @param <T> tipo generico
     * @param worker worker
     * @param workerListeners worker listeners
     */
    @SuppressWarnings("unchecked")
    private static void finishListeners(@SuppressWarnings("rawtypes") final Worker worker,
            final WorkerListener[] workerListeners) {
        if (workerListeners != null) {
            for (final WorkerListener workerListener : workerListeners) {
                workerListener.onFinish(worker);
            }
        }

    }

    /**
     * The Class SimpleService.
     *
     * @param <T> tipo generico
     */
    private final class SimpleService<T> extends Service<T> {

        /** The callable. */
        private final CallableTask<T> callable;

        /** The execution state control. */
        private final ExecutionStateControl<T> executionStateControl;

        /** The worker listeners. */
        private final WorkerListener[] workerListeners;

        /** The task. */
        private Task<T> task;

        /**
         * Instancia un nuevo simple service.
         * @param callable callable
         * @param executionStateControl execution state control
         * @param workerListeners worker listeners
         */
        public SimpleService(final CallableTask<T> callable, final ExecutionStateControl<T> executionStateControl,
                final WorkerListener... workerListeners) {
            super();
            this.callable = callable;
            this.executionStateControl = executionStateControl;
            this.workerListeners = workerListeners;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Task<T> createTask() {
            this.task = new SimpleTask<>(this.callable, this.executionStateControl, this.workerListeners);
            return this.task;
        }

        /**
         * Obtiene task.
         * @return task
         */
        public Task<T> getTask() {
            return this.task;
        }

    }

    /**
     * The Class SimpleTask.
     *
     * @param <T> tipo generico
     */
    private final class SimpleTask<T> extends Task<T> {

        /** The callable. */
        private final CallableTask<T> callable;

        /** The worker listeners. */
        private final WorkerListener[] workerListeners;

        /**
         * Instancia un nuevo simple task.
         * @param callable callable
         * @param executionStateControl execution state control
         * @param workerListeners worker listeners
         */
        public SimpleTask(final CallableTask<T> callable, final ExecutionStateControl<T> executionStateControl,
                final WorkerListener... workerListeners) {
            super();
            this.callable = callable;
            this.workerListeners = workerListeners;
            if (executionStateControl != null) {
                setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(final WorkerStateEvent arg0) {

                        executionStateControl.onSucceeded(arg0, getValue());
                    }
                });
                setOnFailed(new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(final WorkerStateEvent arg0) {
                        executionStateControl.onFailed(arg0, exceptionProperty().get());
                    }
                });
                setOnCancelled(new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(final WorkerStateEvent arg0) {
                        executionStateControl.onCancelled(arg0);
                    }
                });
                setOnRunning(new EventHandler<WorkerStateEvent>() {

                    @Override
                    public void handle(final WorkerStateEvent arg0) {
                        executionStateControl.onRunning(arg0);
                    }
                });

            }

        }

        @Override
        protected void done() {
            super.done();

            // finish
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    finishListeners(SimpleTask.this, SimpleTask.this.workerListeners);
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected T call() throws Exception {
            final Future<T> result = this.callable.call(new TaskControl() {

                @Override
                public void updateTitle(final String title) {
                    SimpleTask.this.updateTitle(title);
                }

                @Override
                public void updateProgress(final long workDone, final long max) {
                    SimpleTask.this.updateProgress(workDone, max);
                }

                @Override
                public void updateProgress(final double workDone, final double max) {
                    SimpleTask.this.updateProgress(workDone, max);

                }

                @Override
                public void updateMessage(final String message) {
                    SimpleTask.this.updateMessage(message);

                }

                @Override
                public boolean isCancelled() {
                    return SimpleTask.this.isCancelled();
                }
            });
            return result.get();

        }

    }

    /**
     * Establece executor.
     * @param executor nuevo executor
     */
    public void setExecutor(final Executor executor) {
        this.executor = executor;
    }

}
