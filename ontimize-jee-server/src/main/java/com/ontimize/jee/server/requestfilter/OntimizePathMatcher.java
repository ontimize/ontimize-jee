package com.ontimize.jee.server.requestfilter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class OntimizePathMatcher {
	private List<AntPathRequestMatcher> matchers = null;

	public OntimizePathMatcher(String[] paterns) {
		this.matchers = new ArrayList<AntPathRequestMatcher>();
		
		for (final String pattern : paterns) {
			this.matchers.add(new AntPathRequestMatcher(pattern));
		}
    }

    public boolean matches(HttpServletRequest request) {
    	return this.matchers.stream().anyMatch(m -> (m.matches(request)));
    }
}
