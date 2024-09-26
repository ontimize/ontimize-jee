package com.ontimize.jee.server.session;

import java.util.Map;
import java.util.Set;

import org.springframework.session.Session;
import org.springframework.session.MapSessionRepository;

import com.ontimize.jee.common.tools.ReflectionTools;

public class StatisticsMapSessionRepository extends MapSessionRepository {

    public StatisticsMapSessionRepository(Map<String, Session> sessions) {
        super(sessions);
    }

    public Set<String> getActiveSessions() {
        return ((Map<String, ?>) ReflectionTools.getFieldValue(this, "sessions")).keySet();
    }

}
