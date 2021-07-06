package com.ontimize.jee.common.security;

import com.ontimize.jee.common.util.calendar.TimePeriod;

/**
 * The class <code>TableFormPermission</code> defines the structure that table permissions must
 * have. For example, these permissions can be used to define the visibility of the table components
 * based on client permissions.
 *
 * @author Imatia Innovation
 */
public class TableFormPermission extends FormPermission {

    protected String type = null;

    protected String columnName = null;

    /**
     * Creates a new table permission
     * @param archiveName the archive name in case the component has a related archive
     * @param permissionName the permission name
     * @param componentAttr the attribute of the component
     * @param restricted restricted condition
     * @param period period condition
     * @param type the type
     */
    public TableFormPermission(String archiveName, String permissionName, String componentAttr, boolean restricted,
            TimePeriod period, String type) {
        super(archiveName, permissionName, componentAttr, restricted, period);
        this.type = type;
    }

    /**
     * Creates a new table permission
     * @param archiveName the archive name in case the component has a related archive
     * @param permissionName the permission name
     * @param componentAttr the attribute of the component
     * @param restricted restricted condition
     * @param period period condition
     * @param type the type
     * @param columnName the columnName for hiding table columns with permissions
     *
     * @since 5.2077EN-0.2
     */
    public TableFormPermission(String archiveName, String permissionName, String componentAttr, boolean restricted,
            TimePeriod period, String type,
            String columnName) {
        super(archiveName, permissionName, componentAttr, restricted, period);
        this.type = type;
        this.columnName = columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof TableFormPermission) {
            return this.name.equals(((TableFormPermission) o).getPermissionName())
                    && this.attr.equals(((TableFormPermission) o).getAttribute()) && this.archive
                        .equals(((TableFormPermission) o).getArchiveName())
                    && (this.type != null) && this.type
                        .equals(((TableFormPermission) o).getType())
                    && ((this.columnName == null) || this.columnName.equals(((TableFormPermission) o).getColumnName()));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getType() {
        return this.type;
    }

    public String getColumnName() {
        return this.columnName;
    }

}
