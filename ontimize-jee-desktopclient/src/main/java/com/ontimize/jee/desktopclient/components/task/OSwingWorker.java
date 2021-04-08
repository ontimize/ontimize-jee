package com.ontimize.jee.desktopclient.components.task;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.ontimize.jee.desktopclient.components.WindowTools;

/**
 * Extension del {@link SwingWorker} para anadirle un escuchador de estado.
 *
 * @param <T> the generic type
 * @param <V> the value type
 * @author joaquin.romero
 */
public abstract class OSwingWorker<T, V> extends SwingWorker<T, V> {

    /** The v worker status update listener. */
    protected List<WorkerStatusUpdateListener> vWorkerStatusUpdateListener;

    /**
     * Constructor.
     */
    public OSwingWorker() {
        super();
        this.vWorkerStatusUpdateListener = new ArrayList<>(1);
    }

    /**
     * Dispara un evento de actualizacion de estado.
     * @param info the info
     */
    public void fireStatusUpdate(final WorkerStatusInfo info) {
        synchronized (this.vWorkerStatusUpdateListener) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    for (WorkerStatusUpdateListener listener : OSwingWorker.this.vWorkerStatusUpdateListener) {
                        listener.statusUpdated(OSwingWorker.this, info);
                    }
                }
            });
        }
    }

    /**
     * Anade un escuchador de actualizacion de estado.
     * @param listener the listener
     */
    public void addStatusUpdateListener(WorkerStatusUpdateListener listener) {
        synchronized (this.vWorkerStatusUpdateListener) {
            this.vWorkerStatusUpdateListener.add(listener);
        }
    }

    /**
     * Elimina un escuchador de cambio de estado.
     * @param listener the listener
     */
    public void removeStatusUpdateListener(WorkerStatusUpdateListener listener) {
        synchronized (this.vWorkerStatusUpdateListener) {
            this.vWorkerStatusUpdateListener.remove(listener);
        }
    }

    /**
     * Ejecuta el swingworker mostrando una ventana de progreso bloqueante.
     * @param referenceComponent the reference component
     */
    public void executeOperation(Component referenceComponent) {
        this.executeOperation(referenceComponent, true);
    }

    /**
     * Ejecuta el swingworker mostrando una ventana de progreso bloqueante.
     * @param referenceComponent the reference component
     * @param cancellable the cancellable
     */
    public void executeOperation(Component referenceComponent, boolean cancellable) {
        Window container = WindowTools.getWindowAncestor(referenceComponent);
        if (container == null) {
            container = WindowTools.getActiveWindow();
        }
        if (!container.isVisible()) {
            Window testWindow = WindowTools.getActiveWindow();
            if (testWindow != null) {
                container = testWindow;
            }

        }
        BlockingProgressIndicator indicator = new BlockingProgressIndicator();
        indicator.startShowRelativeTo(this, (RootPaneContainer) container, true, cancellable);
    }

    /**
     * Envuelve el metodo get del {@link OSwingWorker} para que devuelva directamente la excepcion que
     * se produjo en el doInBackground.
     * @return the t
     * @throws ExecutionException
     * @throws Throwable the throwable
     */
    public T uget() throws ExecutionException {
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException error) {
            throw new ExecutionException(error);
        }
    }

}
