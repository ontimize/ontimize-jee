package com.imatia.jee.server.test.spring.namespace;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.springframework.beans.factory.FactoryBean;

public class FileFilterFactoryBean implements FactoryBean<Collection<FileFilter>> {

	private final List<FileFilter>	filters	= new ArrayList<FileFilter>();

	@Override
	public Collection<FileFilter> getObject() throws Exception {
		return this.filters;
	}

	@Override
	public Class<?> getObjectType() {
		return Collection.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	/**
	 * Go through the list of filters and convert the String ones (the ones that were set with <value> and make them NameFileFilters
	 */
	public void setFilters(Collection<Object> filterList) {
		for (Object o : filterList) {
			if (o instanceof String) {
				this.filters.add(new NameFileFilter(o.toString()));
			} else if (o instanceof FileFilter) {
				this.filters.add((FileFilter) o);
			}
		}
	}

}