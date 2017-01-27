package com.ontimize.jee.server.session;

import java.util.Map;
import java.util.Set;

import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSessionRepository;

import com.ontimize.jee.common.tools.ReflectionTools;

public class StatisticsMapSessionRepository extends MapSessionRepository {


	public StatisticsMapSessionRepository() {
		super();
	}

	public StatisticsMapSessionRepository(Map<String, ExpiringSession> sessions) {
		super(sessions);
	}

	public Set<String> getActiveSessions() {
		return ((Map<String, ?>) ReflectionTools.getFieldValue(this, "sessions")).keySet();
	}

}
