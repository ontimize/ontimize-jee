package com.ontimize.jee.common.util.extend;

import org.w3c.dom.NodeList;
import java.util.List;
import java.util.ArrayList;

public class InnerExtendedClientApplicationOperation {

    protected String operation;

    protected String nameAttr;

    protected String valueAttr;

    protected String formAttr;

    protected NodeList extendCode;

    protected List internalOperations;

    public InnerExtendedClientApplicationOperation(String operation, String name, String value, String form) {

        this.operation = operation;
        this.nameAttr = name;
        this.valueAttr = value;
        this.formAttr = form;

        this.internalOperations = new ArrayList();
    }

    public String getFormAttr() {
        return this.formAttr;
    }

    public void setFormAttr(String formAttr) {
        this.formAttr = formAttr;
    }

    public String getNameAttr() {
        return this.nameAttr;
    }

    public void setNameAttr(String nameAttr) {
        this.nameAttr = nameAttr;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getValueAttr() {
        return this.valueAttr;
    }

    public void setValueAttr(String valueAttr) {
        this.valueAttr = valueAttr;
    }

    public List getInternalOperations() {
        return this.internalOperations;
    }

    public boolean hasInternalOperations() {

        return !this.internalOperations.isEmpty();
    }

    public void addInternalOperation(InnerExtendedClientApplicationOperation internalOperation) {
        this.internalOperations.add(internalOperation);
    }

    public NodeList getExtendCode() {
        return this.extendCode;
    }

    public void setExtendCode(NodeList extendCode) {
        this.extendCode = extendCode;
    }

}
