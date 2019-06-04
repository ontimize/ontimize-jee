package com.ontimize.jee.server.services.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.session.ExpiringSession;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Component;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.session.SessionDto;
import com.ontimize.jee.server.session.StatisticsMapSessionRepository;

@Component
@Lazy(value = true)
public class SessionHelper implements ApplicationContextAware {

	private static final Logger		LOG					= LoggerFactory.getLogger(SessionHelper.class);

	private static final String		REDIS_KEY_PREFIX	= "spring-security-sessions:";

	@Autowired(required = false)
	private SessionRepository<?>	sessionRepo;

	private ApplicationContext		applicationContext;

	public Collection<SessionDto> getActiveSessions() throws OntimizeJEEException {
		if (this.sessionRepo == null) {
			return Collections.EMPTY_LIST;
		}
		Collection<SessionDto> res = new ArrayList<>();
		if (this.sessionRepo instanceof StatisticsMapSessionRepository) {
			Collection<String> sessionList = ((StatisticsMapSessionRepository) this.sessionRepo).getActiveSessions();
			for (String sesId : sessionList) {
				ExpiringSession session = (ExpiringSession) this.sessionRepo.getSession(sesId);
				if (session != null) {
					res.add(this.toDto(session));
				}
			}
		} else if (this.sessionRepo instanceof RedisOperationsSessionRepository) {
			RedisOperations<String, Object> redisOps = this.applicationContext.getBean(RedisOperations.class);
			Set<String> keys = redisOps.keys(SessionHelper.REDIS_KEY_PREFIX + "*");
			SessionHelper.LOG.info("Number of active sessions: " + keys.size());
			for (String key : keys) {
				String id = key.substring(SessionHelper.REDIS_KEY_PREFIX.length());
				ExpiringSession session = (ExpiringSession) this.sessionRepo.getSession(id);
				if (session != null) {
					res.add(this.toDto(session));
				}
			}
		} else {
			throw new OntimizeJEEException("Unknow session repository " + this.sessionRepo);
		}
		return res;

	}

	private SessionDto toDto(ExpiringSession session) {
		HashMap<String, Object> sessionAttrs = new HashMap<>(session.getAttributeNames().size());
		for (String attrName : session.getAttributeNames()) {
			Object attrValue = session.getAttribute(attrName);
			sessionAttrs.put(attrName, attrValue);
		}

		return new SessionDto(session.getId(), sessionAttrs, session.getLastAccessedTime(), session.getCreationTime(), session.getMaxInactiveIntervalInSeconds());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}