package com.ontimize.jee.server.dao.jdbc;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.ontimize.jee.server.dao.common.ConfigurationFile;

/**
 * @author Enrique Alvarez Pereira <enrique.alvarez@imatia.com>
 */
@Lazy
@Repository(value = "RepositoryUnityTestDao")
@ConfigurationFile(configurationFile = "base-dao/RepositoryUnityTestDao.xml",
        configurationFilePlaceholder = "base-dao/placeholders.properties")
public class RepositoryUnityTestDao extends OntimizeJdbcDaoSupport {

    public static final String INNER_JOIN = "INNER_JOIN";

    public static final String NESTED_TABLES = "NESTED_TABLES";

    public static final String AMBIGUOUS_COLUMNS = "AMBIGUOUS_COLUMNS";

    public static final String ORDERBY = "ORDERBY";

    public static final String FUNCTION_COLUMNS = "FUNCTION_COLUMNS";

    public static final String ORDERCOLUMN = "ORDERCOLUMN";

    public static final String WHERE_CONCAT = "WHERE_CONCAT";

    public RepositoryUnityTestDao() {
        super();
    }

}
