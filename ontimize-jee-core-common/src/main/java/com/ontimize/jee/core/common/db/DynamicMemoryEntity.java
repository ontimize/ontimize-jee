package com.ontimize.jee.core.common.db;

import com.ontimize.jee.core.common.dto.EntityResult;

public interface DynamicMemoryEntity {

    public void setValue(EntityResult data);

    public void clear();

}
