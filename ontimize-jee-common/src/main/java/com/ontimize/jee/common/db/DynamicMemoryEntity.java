package com.ontimize.jee.common.db;

import com.ontimize.jee.common.dto.EntityResult;

public interface DynamicMemoryEntity {

    public void setValue(EntityResult data);

    public void clear();

}
