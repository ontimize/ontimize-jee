package com.imatia.jee.server.test.remoteoperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.callback.CallbackWrapperMessage;
import com.ontimize.jee.server.services.remoteoperation.AbstractRemoteOperation;

public class TestRemoteOperation extends AbstractRemoteOperation {

	private static final Logger	logger		= LoggerFactory.getLogger(TestRemoteOperation.class);
	private boolean				cancelled	= false;

	@Override
	public void init(Object parameters) {
		// Do nothing
	}

	@Override
	public Object onCustomMessageReceived(CallbackWrapperMessage msg) {
		TestRemoteOperation.logger.warn("custom message received");
		return null;
	}

	@Override
	public void onCancelReceived() {
		this.cancelled = true;
	}

	@Override
	public Object execute() {
		for (int i = 0; i < 10; i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException error) {
				TestRemoteOperation.logger.error(null, error);
			}
			if (this.cancelled) {
				TestRemoteOperation.logger.error("Task cancelled");
				return null;
			}
			this.operationStep(i, 10, 0, "test iteration " + i, null);
		}
		return null;
	}

}
