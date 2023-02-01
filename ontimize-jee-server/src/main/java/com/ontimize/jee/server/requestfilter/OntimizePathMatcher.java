package com.ontimize.jee.server.requestfilter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class OntimizePathMatcher {
	private List<AntPathRequestMatcher> matchers = new ArrayList<>();

	public OntimizePathMatcher(String[] paterns) {
		for (final String pattern : paterns) {
			this.matchers.add(new AntPathRequestMatcher(pattern));
		}
    }

    public boolean matches(HttpServletRequest request) {
    	return this.matchers.stream().anyMatch(m -> (m.matches(request)));
    }
}
