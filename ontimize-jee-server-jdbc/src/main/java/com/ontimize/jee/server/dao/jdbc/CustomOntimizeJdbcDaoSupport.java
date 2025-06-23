package com.ontimize.jee.server.dao.jdbc;

import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.handler.SQLStatementHandler;
import com.ontimize.jee.common.dto.EntityResult;

/**
 * TODO TRADUCIR
 * Esta clase permite extender la funcionalidad de {@link OntimizeJdbcDaoSupport}, para permitir que se puedan ejecutar
 * consultas sobre tablas temporales.
 * Esta extensión permite dotar al DAO de la capacidad de, para una consulta
 * específica, crear una tabla temporal, poblarla, consultar dicha tabla temporal y eliminarla. Tiene soporte para todos
 * los handles de base de datos que maneja Ontimize
 */
public class CustomOntimizeJdbcDaoSupport extends OntimizeJdbcDaoSupport {
    /**
     * Constructor sin parámetros
     */
    public CustomOntimizeJdbcDaoSupport() {
    }

    /**
     * Constructor con parámentros
     * @param configurationFile Ruta del fichero *.xml del DAO
     * @param configurationFilePlaceholder Ruta del fichero de placeholders que puede contener el fichero *.xml del DAO
     */
    public CustomOntimizeJdbcDaoSupport(String configurationFile, String configurationFilePlaceholder) {
        super(configurationFile, configurationFilePlaceholder);
    }

    /**
     * Realiza la consulta deseada según el queryId suministrado
     * @param tableName
     */
    public EntityResult queryTemporalTable(String tableName, SQLStatementBuilder.SQLStatement selectSql, SQLStatementHandler.TemporalTableScope scope){
        //TODO CREAR SELECTSQL
        createTemporalTable(tableName,selectSql,scope);
        //TODO CONSULTA QUERY queryTemporalTable
        dropTemporalTable(tableName, scope);
        return null;
    }

    /**
     * Crea la tabla temporal
     * @param tableName
     */
    public void createTemporalTable(String tableName, SQLStatementBuilder.SQLStatement selectSql, SQLStatementHandler.TemporalTableScope scope){
        this.getStatementHandler().createTemporalTableStatement(tableName, selectSql, scope);
        //TODO EJECUTAR STATEMENT
    }

    /**
     * Elimina la tabla temporal
     * @param tableName
     */
    public void dropTemporalTable(String tableName, SQLStatementHandler.TemporalTableScope scope){
        this.getStatementHandler().dropTemporalTableStatement(tableName, scope);
        //TODO EJECUTAR STATEMENT
    }
}
