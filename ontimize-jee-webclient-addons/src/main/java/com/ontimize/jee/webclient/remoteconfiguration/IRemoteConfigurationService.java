package com.ontimize.jee.webclient.remoteconfiguration;

import java.util.List;
import java.util.Map;

import com.ontimize.dto.EntityResult;

/**
 * Remote configuration service interface
 */
public interface IRemoteConfigurationService {

    public EntityResult remoteConfigurationQuery(Map<?, ?> keysValues, List<String> attributes);

    public EntityResult remoteConfigurationInsert(Map<?, ?> attributesValues);

    public EntityResult remoteConfigurationUpdate(Map<?, ?> attributesValues, Map<?, ?> keysValues);

    public EntityResult remoteConfigurationDelete(Map<?, ?> keysValues);

}
