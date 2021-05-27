package com.ontimize.jee.common.util.extend;

import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.List;

public class ExtendedClientApplicationOperation {

    public static final int UNIQUE_OP = 1;

    public static final int MANAGER_OP = 2;

    protected int type;

    protected String operation;

    protected String parentAttr;

    protected String idAttr;

    protected List internalOperations;

    protected NodeList extendCode;

    public ExtendedClientApplicationOperation(int type, String operation, String attr) {
        this.type = type;
        this.operation = operation;

        switch (type) {
            case UNIQUE_OP:
                this.parentAttr = attr;
                break;

            case MANAGER_OP:
                this.idAttr = attr;
                break;
            default:
                break;
        }

        this.internalOperations = new ArrayList();
    }

    public String getIdAttr() {
        return this.idAttr;
    }

    public void setIdAttr(String idAttr) {
        this.idAttr = idAttr;
    }

    public List getInternalOperations() {
        return this.internalOperations;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getParentAttr() {
        return this.parentAttr;
    }

    public void setParentAttr(String parentAttr) {
        this.parentAttr = parentAttr;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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
