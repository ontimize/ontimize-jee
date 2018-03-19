package com.ontimize.jee.server.session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

/**
 * The Class EmbeddedRedisServerManager.
 */
public class EmbeddedRedisServerManager implements InitializingBean, Lifecycle {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#start()
	 */
	@Override
	public void start() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#stop()
	 */
	@Override
	public void stop() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return false;
	}

}