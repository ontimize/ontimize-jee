package com.ontimize.jee.server.dao.jdbc;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.ontimize.jee.common.db.SQLStatementBuilder.ExtendedSQLConditionValuesProcessor;
import com.ontimize.jee.common.db.handler.HSQLDBSQLStatementHandler;
import com.ontimize.jee.common.db.handler.SQLStatementHandler;
import com.ontimize.jee.server.dao.common.INameConvention;

/**
 * @author Enrique Alvarez Pereira <enrique.alvarez@imatia.com>
 */
@Configuration
@PropertySource("inmemory-database.properties")
@EnableTransactionManagement
public class InMemoryDatabaseConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource inMemDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("hsqldb.driverClassName"));
        dataSource.setUrl(env.getProperty("hsqldb.url"));
        dataSource.setUsername(env.getProperty("hsqldb.user"));

        return dataSource;
    }

    @Bean
    public RepositoryUnityTestDao repositoryUnityTest() {
        return new RepositoryUnityTestDao();
    }

    @Bean
    public InMemoryDatabaseStructure inMemoryDatabaseStructure() {
        return new InMemoryDatabaseStructure();
    }

    @Bean("testSqlStatementHandler")
    public SQLStatementHandler hsqldbSQLStatementHandler() {
        SQLStatementHandler handler = new HSQLDBSQLStatementHandler();
        handler.setSQLConditionValuesProcessor(this.extendedSQLConditionValuesProcessor());
        return handler;
    }

    @Bean
    public ExtendedSQLConditionValuesProcessor extendedSQLConditionValuesProcessor() {
        return new ExtendedSQLConditionValuesProcessor(false, false);
    }

    @Bean("name_convention")
    public INameConvention lowerNameConvention() {
        return new com.ontimize.jee.server.dao.common.LowerCaseNameConvention();
    }

}
