package com.ontimize.jee.common.services;

public class ServiceTools {

    /**
     * Extrae el nombre del servicio al que se quiere invocar a partir del nombre de la entidad
     * configurado. El nombre de la enteidad debería tener la forma <servicename>.<servicemethodprefix>.
     * @param entityName
     * @return
     */
    public static String extractServiceFromEntityName(String entityName) {
        int dotIdx = entityName.lastIndexOf('.');
        if (dotIdx < 0) {
            return entityName;
        } else {
            return entityName.substring(0, dotIdx);
        }
    }

    /**
     * Extrae el prefijo del método del servicio al que se quiere invocar a partir del nombre de la
     * entidad configurado. El nombre de la enteidad debería tener la forma
     * <servicename>.<servicemethodprefix>.
     * @param entityName
     * @return
     */
    public static String extractServiceMethodPrefixFromEntityName(String entityName) {
        int dotIdx = entityName.lastIndexOf('.');
        int parenthesisIdx = entityName.lastIndexOf('(');
        if (dotIdx < 0) {
            return "";
        } else if (parenthesisIdx < 0) {
            return entityName.substring(dotIdx + 1);
        } else {
            return entityName.substring(dotIdx + 1, parenthesisIdx);
        }
    }

    /**
     * Extract query id from entity name.
     * @param entityName the entity name
     * @return the string
     */
    public static String extractQueryIdFromEntityName(String entityName) {
        int parenthesisBeginIdx = entityName.lastIndexOf('(');
        int parenthesisEndIdx = entityName.lastIndexOf(')');
        if ((parenthesisBeginIdx < 0) || (parenthesisEndIdx < 0)) {
            return null;
        } else {
            return entityName.substring(parenthesisBeginIdx + 1, parenthesisEndIdx);
        }
    }

}
