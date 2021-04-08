package com.ontimize.jee.desktopclient.components.servermanagement.managers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import com.ontimize.annotation.FormComponent;
import com.ontimize.gui.BasicInteractionManager;
import com.ontimize.gui.Form;
import com.ontimize.gui.MessageDialog;
import com.ontimize.gui.button.Button;
import com.ontimize.gui.field.DateDataField;
import com.ontimize.gui.manager.IFormManager;
import com.ontimize.gui.table.Table;
import com.ontimize.jee.common.services.servermanagement.IServerManagementService;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

/**
 * The Class IMLiveLogConsole.
 */
public class IMRequest extends BasicInteractionManager {

    @FormComponent(attr = "DETAILS")
    protected Table tDetails;

    @FormComponent(attr = "RESULTS")
    protected Table tResults;

    @FormComponent(attr = "BEFORE_DATE")
    protected DateDataField beforeDate;

    @FormComponent(attr = "AFTER_DATE")
    protected DateDataField afterDate;

    @FormComponent(attr = "B_QUERY")
    protected Button bQuery;

    @FormComponent(attr = "B_REQUEST")
    protected Button bRequest;

    private IServerManagementService serverManagement;

    public IMRequest() {
        super();
    }

    @Override
    public void registerInteractionManager(Form f, IFormManager gf) {
        super.registerInteractionManager(f, gf);
        this.managedForm.setFormTitle("Request statistics");
        this.serverManagement = BeansFactory.getBean(IServerManagementService.class);
        if (this.tResults != null) {
            this.tResults.setValue(this.serverManagement.getStatistics());
        }
        if (this.bRequest != null) {
            this.bRequest.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int selectedRow = IMRequest.this.tResults.getSelectedRow();
                    if (selectedRow != -1) {
                        Hashtable selectedRowData = IMRequest.this.tResults.getSelectedRowData();
                        String serviceName = ((Vector<String>) selectedRowData.get("SERVICE_NAME")).get(0);
                        String methodName = ((Vector<String>) selectedRowData.get("METHOD_NAME")).get(0);
                        IMRequest.this.tDetails
                            .setValue(IMRequest.this.serverManagement.getServiceStatistics(serviceName, methodName,
                                    (Date) IMRequest.this.beforeDate.getDateValue(),
                                    (Date) IMRequest.this.afterDate.getValue()));
                    } else {
                        MessageDialog.showErrorMessage(IMRequest.this.managedForm.getJDialog(), "E_SELECTED_METHOD");
                    }
                }
            });
        }
        if (this.bQuery != null) {
            this.bQuery.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (IMRequest.this.tResults != null) {
                        IMRequest.this.tResults.setValue(IMRequest.this.serverManagement.getStatistics());
                    }
                }
            });
        }

    }

}
