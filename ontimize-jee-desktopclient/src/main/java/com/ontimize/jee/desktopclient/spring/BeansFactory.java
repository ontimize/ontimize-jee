/**
 */
package com.ontimize.jee.desktopclient.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

/**
 * Clase de ayuda para acceder a beans de spring. Debe iniciarse BeansFactory.init en el lanzador
 * del cliente.
 *
 */
public final class BeansFactory {

    /** Fichero de configuración por defecto de spring */
    public static final String DEFAULT_SPRING_CONFIG_FILE = "classpath*:spring-config.xml";

    public static final Logger logger = LoggerFactory.getLogger(BeansFactory.class);

    /** referencia al {@link ApplicationContext} */
    private static GenericXmlApplicationContext applicationContext = null;

    /**
     * Constructor del singleton
     */
    private BeansFactory() {
        // SINGLETON
    }

    /**
     * Inicializa la factoria.
     * @param springConfigurationFile ficheros de configuración para iniciar el contexto. En caso de
     *        ser null se utiliza DEFAULT_SPRING_CONFIG_FILE
     */
    public static void init(String[] springConfigurationFile) {
        if (BeansFactory.applicationContext == null) {
            List<String> contextFiles = (springConfigurationFile == null) || (springConfigurationFile.length == 0)
                    ? new ArrayList<>() : new ArrayList<>(
                            Arrays.asList(springConfigurationFile));
            if (contextFiles.size() == 0) {
                contextFiles.add(BeansFactory.DEFAULT_SPRING_CONFIG_FILE);
            }
            BeansFactory.applicationContext = new GenericXmlApplicationContext(
                    contextFiles.toArray(new String[contextFiles.size()]));
        }
    }

    public static void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        BeansFactory.applicationContext.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * Devuelve el {@link ApplicationContext} de spring
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return BeansFactory.applicationContext;
    }

    /**
     * Devuelve la referencia a un bean.
     * @param <T> the generic type
     * @param type the type
     * @return the bean
     */
    public static <T> T getBean(Class<T> type) {
        return BeansFactory.applicationContext.getBean(type);
    }

    /**
     * Devuelve la referencia a un bean.
     * @param <T> the generic type
     * @param beanName the bean name
     * @param type the type
     * @return the bean
     */
    public static <T> T getBean(String beanName, Class<T> type) {
        return BeansFactory.applicationContext.getBean(beanName, type);
    }

    /**
     * Devuelve la referencia aun bean.
     * @param beanName the bean name
     * @return the bean
     */
    public static Object getBean(String beanName) {
        return BeansFactory.applicationContext.getBean(beanName);
    }

}
