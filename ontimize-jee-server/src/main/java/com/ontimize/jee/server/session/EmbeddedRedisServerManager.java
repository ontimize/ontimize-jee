package com.ontimize.jee.server.session;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;

// import redis.embedded.RedisServer;

/**
 * The Class EmbeddedRedisServerManager.
 */
public class EmbeddedRedisServerManager implements InitializingBean, Lifecycle {

	/** The redis server. */
	@Autowired
	// private RedisServer redisServer;

	/**
	 * Sets the redis server.
	 *
	 * @param redisServer
	 *            the new redis server
	 */
	// public void setRedisServer(RedisServer redisServer) {
	// this.redisServer = redisServer;
	// }

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// this.redisServer.start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#start()
	 */
	@Override
	public void start() {

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#stop()
	 */
	@Override
	public void stop() {
		// this.redisServer.stop();

	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.Lifecycle#isRunning()
	 */
	@Override
	public boolean isRunning() {
		// return this.redisServer.isActive();
		return false;
	}

}