package com.ontimize.jee.webclient.remoteconfiguration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ontimize.dto.EntityResult;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@Service("RemoteConfigurationService")
public class RemoteConfigurationService implements IRemoteConfigurationService {

    @Autowired
    DefaultOntimizeDaoHelper daoHelper;

    @Autowired
    protected IRemoteConfigurationDao remoteConfigurationDao;

    @Override
    public EntityResult remoteConfigurationQuery(Map<?, ?> keysValues, List<String> attributes) {
        return this.daoHelper.query(this.remoteConfigurationDao, keysValues, attributes);
    }

    @Override
    public EntityResult remoteConfigurationInsert(Map<?, ?> attributesValues) {
        return this.daoHelper.insert(this.remoteConfigurationDao, attributesValues);
    }

    @Override
    public EntityResult remoteConfigurationUpdate(Map<?, ?> attributesValues, Map<?, ?> keysValues) {
        return this.daoHelper.update(this.remoteConfigurationDao, attributesValues, keysValues);
    }

    @Override
    public EntityResult remoteConfigurationDelete(Map<?, ?> keysValues) {
        return this.daoHelper.delete(this.remoteConfigurationDao, keysValues);
    }

}
