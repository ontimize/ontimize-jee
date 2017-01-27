package com.ontimize.jee.server.dao.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Annotation ConfigurationFile.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationFile {

	/**
	 * The value indicate the path to the configuration file.
	 *
	 * @return the configuration file path
	 */
	String configurationFile();

	/**
	 * The placeholder indicate the path to the placeholder file for replacement.
	 *
	 * @return the configuration file path
	 */
	String configurationFilePlaceholder() default "";
}
