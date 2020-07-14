package com.ontimize.jee.server.services.remoteoperation;

/**
 * The Class AbstractRemoteOperation.
 */
public abstract class AbstractRemoteOperation implements IRemoteOperation {

    /** The listener. */
    private IRemoteOperationListener listener;

    /**
     * Instantiates a new abstract remote operation.
     */
    public AbstractRemoteOperation() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            Object res = this.execute();
            this.listener.onOperationFinished(res);
        } catch (Exception ex) {
            this.listener.onOperationError(ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.jee.server.services.remoteoperation.IRemoteOperation#setListener
     * (com.ontimize.jee.server.services.remoteoperation.IRemoteOperationListener)
     */
    @Override
    public void setListener(IRemoteOperationListener listener) {
        this.listener = listener;
    }

    /**
     * Operation step.
     * @param currentStep the current step
     * @param maxSteps the max steps
     * @param estimatedTime the estimated time
     * @param message the message
     * @param parameters the parameters
     */
    protected void operationStep(int currentStep, int maxSteps, long estimatedTime, String message, Object parameters) {
        this.listener.onOperationStep(currentStep, maxSteps, estimatedTime, message, parameters);
    }

}
