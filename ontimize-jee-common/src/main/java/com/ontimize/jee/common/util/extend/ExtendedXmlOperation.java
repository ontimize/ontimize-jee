package com.ontimize.jee.common.util.extend;

import org.w3c.dom.NodeList;

public class ExtendedXmlOperation {

    protected String operation;

    protected String attrName;

    protected String parentContainerType;

    protected int levelsToParent = 0;

    protected NodeList extendCode;

    public ExtendedXmlOperation() {
    }

    public ExtendedXmlOperation(String operation, String attrName, String parentContainerType, int levelsToParent,
            NodeList extendCode) {
        this.operation = operation;
        this.attrName = attrName;
        this.parentContainerType = parentContainerType;
        this.levelsToParent = levelsToParent;
        this.extendCode = extendCode;
    }

    public String getAttrName() {
        return this.attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public NodeList getExtendCode() {
        return this.extendCode;
    }

    public void setExtendCode(NodeList extendCode) {
        this.extendCode = extendCode;
    }

    public int getLevelsToParent() {
        return this.levelsToParent;
    }

    public void setLevelsToParent(int levelsToParent) {
        this.levelsToParent = levelsToParent;
    }

    public String getOperation() {
        return this.operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getParentContainerType() {
        return this.parentContainerType;
    }

    public void setParentContainerType(String parentContainerType) {
        this.parentContainerType = parentContainerType;
    }

}
