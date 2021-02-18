package com.ontimize.jee.server.services.formprovider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.ontimize.dto.EntityResult;
import com.ontimize.jee.common.services.formprovider.IFormProviderService;
import com.ontimize.jee.common.tools.CheckingTools;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;

public class FormProviderEngine implements IFormProviderService, InitializingBean {

    private String formProviderIdColumn = "";

    private String formProviderFormNameColumn = "";

    private String formProviderFormXMLColumn = "";

    private IOntimizeDaoSupport daoFormProvider;

    @Override
    public String getXMLForm(String form) throws Exception {

        Map<String, String> filter = new HashMap<String, String>();

        if ((form != null) && !this.formProviderFormNameColumn.isEmpty() && !this.formProviderFormXMLColumn.isEmpty()) {
            filter.put(this.formProviderFormNameColumn, form);
            EntityResult toret = this.daoFormProvider.query(filter,
                    Arrays.asList(new String[] { this.formProviderFormXMLColumn }), (List<String>) null, (String) null);

            if ((toret.getCode() == EntityResult.OPERATION_WRONG) || (toret.calculateRecordNumber() > 1)) {
                throw new Exception("ERROR_RETRIEVING_A_FORM_FROM_DATABASE");
            }

            if (toret.isEmpty()) {
                return null;
            }

            return toret.getRecordValues(0).get(this.formProviderFormXMLColumn).toString();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        CheckingTools.failIfEmptyString(this.formProviderIdColumn,
                "The identifier column does not exist in the database for the form provider table.");
        CheckingTools.failIfEmptyString(this.formProviderFormNameColumn,
                "The form name column does not exist in the database for the form provider table.");
        CheckingTools.failIfEmptyString(this.formProviderFormXMLColumn,
                "The xml of form column does not exist in the database for the form provider table.");
        CheckingTools.failIfNull(this.daoFormProvider, "There is no class defined for the form provider dao.");
    }

    /**
     * Get form provider dao
     * @return the Form Provider Dao
     */
    public IOntimizeDaoSupport getDaoFormProvider() {
        return this.daoFormProvider;
    }

    /**
     * Set the form provider
     * @param daoFormProvider {@link IOntimizeDaoSupport} Form provider to set
     */
    public void setDaoFormProvider(IOntimizeDaoSupport daoFormProvider) {
        this.daoFormProvider = daoFormProvider;
    }

    /**
     * Gets the id column of the form provider
     * @return {@link String} The id column of the id provider
     */
    public String getFormProviderIdColumn() {
        return this.formProviderIdColumn;
    }

    /**
     * Sets the id column of the form provider
     * @param formProviderIdColumn {@link String} The id column of the form provider
     */
    public void setFormProviderIdColumn(String formProviderIdColumn) {
        this.formProviderIdColumn = formProviderIdColumn;
    }

    /**
     * Gets the form name column of the form provider
     * @return {@link String} The form name column of the form provider
     */
    public String getFormProviderFormNameColumn() {
        return this.formProviderFormNameColumn;
    }

    /**
     * Sets the form name column of the form provider
     * @param formProviderFormNameColumn {@link String} The form name column of the form provider
     */
    public void setFormProviderFormNameColumn(String formProviderFormNameColumn) {
        this.formProviderFormNameColumn = formProviderFormNameColumn;
    }

    /**
     * Gets the form xml column of the form provider
     * @return {@link String} the form xml column of the form provider
     */
    public String getFormProviderFormXMLColumn() {
        return this.formProviderFormXMLColumn;
    }

    /**
     * Sets the form xml column of the form provider
     * @param formProviderFormXMLColumn {@link String} The form xml column of the form provider
     */
    public void setFormProviderFormXMLColumn(String formProviderFormXMLColumn) {
        this.formProviderFormXMLColumn = formProviderFormXMLColumn;
    }

}
