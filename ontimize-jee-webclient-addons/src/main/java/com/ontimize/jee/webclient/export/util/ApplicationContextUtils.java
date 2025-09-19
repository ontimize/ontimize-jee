package com.ontimize.jee.webclient.export.util;

import com.ontimize.jee.server.rest.ORestController;
import com.ontimize.jee.webclient.export.exception.ExportException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ApplicationContextUtils implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private static ApplicationContextUtils instance;
    
    public ApplicationContextUtils() {
        //no-op
    }
    
    public Object getServiceBean(final String serviceName , final String servicePath) throws ExportException {

        Object serviceBean = null;
        
        //Method 1. Retrieve bean from controller 'path'
        if(!StringUtils.isBlank(servicePath)) {
            RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                    .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> requestMap = requestMappingHandlerMapping.getHandlerMethods();

            List<HandlerMethod> requestMapHandlerMethodList = requestMap.keySet().stream()
                    .filter(key -> key.getActivePatternsCondition().toString().equals("[" + servicePath + "/{name}/search]"))
                    .map(requestMap::get)
                    .collect(Collectors.toList());

            if (requestMapHandlerMethodList.size() == 1) {
                Class<?> restControllerBeanType = requestMapHandlerMethodList.get(0).getBeanType();
                serviceBean = getBean(restControllerBeanType);
            }
        }
        
        //Method 2. Retrieve controller from service name and then the service bean
        if(serviceBean == null && !StringUtils.isBlank(serviceName)) {
            String[] beanNamesForType = applicationContext.getBeanNamesForType(ORestController.class);
            List<String> restControllerNames = findCandidates(serviceName, beanNamesForType);
            
            if(restControllerNames.size() > 0) {
                if(restControllerNames.size() == 1) {
                    serviceBean = getBeanForName(restControllerNames.get(0));
                } else {
                    String beanName = this.fitBestControllerName(serviceName, restControllerNames);
                    if(!StringUtils.isBlank(beanName)) {
                        serviceBean = getBeanForName(beanName);
                    }
                }
            }
        }
        
        // Method 3. Retrieve bean from service name
        if(serviceBean == null) {
            serviceBean = this.applicationContext.getBean(serviceName.concat("Service"));
        }
        
        if(serviceBean == null) {
            throw new ExportException("Impossible to retrieve service to query data");
        }
        
        return serviceBean;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    
    private Object getBeanForName(final String beanName) {
        Object bean = null;
        ORestController<?> oRestController = applicationContext.getBean(beanName, ORestController.class);
        bean = oRestController.getService();
        return bean;
    }
    private Object getBean(final Class<?> beanClazz) {
        Object bean = null;
        Object restControllerBean = applicationContext.getBean(beanClazz);
        if (restControllerBean instanceof ORestController) {
            bean = ((ORestController<?>) restControllerBean).getService();
        }
        return bean;
    }
    
    private List<String> findCandidates(final String serviceName, String[] beanNamesForType) {
        List<String> restControllerNames = Stream.of(beanNamesForType).filter((item) -> item.startsWith(serviceName.toLowerCase()))
                .collect(Collectors.toList());
        if(restControllerNames.size() == 0) {
            String aux = serviceName.substring(0, serviceName.length()-1);
            if(aux.length() > 3) {
                restControllerNames = findCandidates(aux, beanNamesForType);
            }
        }
        return restControllerNames;
    }
    
    private String fitBestControllerName(final String serviceName, List<String> restControllerNames) {
        String beanName = null;
        for(String name : restControllerNames) {
            String aux = name.replaceAll("RestController", "");
            if(serviceName.toLowerCase().equals(aux.toLowerCase())) {
                beanName = name;
                break;
            }
        }
        if( beanName == null) {
            String aux = serviceName.substring(0, serviceName.length()-1);
            if(aux.length() > 3) {
                beanName = fitBestControllerName(aux, restControllerNames);
            }
        }
        return beanName;
    }
}
