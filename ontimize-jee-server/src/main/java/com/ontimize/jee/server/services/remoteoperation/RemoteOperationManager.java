package com.ontimize.jee.server.services.remoteoperation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.ontimize.jee.server.configuration.OntimizeConfiguration;

@Component("remoteOperationManager")
@Lazy(value = true)
public class RemoteOperationManager implements InitializingBean {

    @Autowired
    private OntimizeConfiguration ontimizeConfiguration;

    public RemoteOperationManager() {
        super();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // el propio engine se registra como escuchador de websocket y atiende directamente las peticiones
        this.ontimizeConfiguration.getRemoteOperationConfiguration().getRemoteOpereationEngine();
    }

    public OntimizeConfiguration getOntimizeConfiguration() {
        return this.ontimizeConfiguration;
    }

    public void setOntimizeConfiguration(OntimizeConfiguration ontimizeConfiguration) {
        this.ontimizeConfiguration = ontimizeConfiguration;
    }

}
