package com.ontimize.jee.common.util.serializer.xml;

import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.gui.SearchValue;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xmlFilterBasicExpression", propOrder = { "leftOperand", "operator", "rightOperand" })
public class XmlFilterBasicExpression {

    @XmlElement(name = "left-operand", required = true)
    protected XmlBasicLeftOperand leftOperand;

    protected String operator;

    @XmlElement(name = "right-operand", required = true)
    protected XmlBasicRightOperand rightOperand;

    public XmlFilterBasicExpression() {
        // TODO Auto-generated constructor stub
    }

    public XmlFilterBasicExpression(BasicExpression bexp) {

        Object lO = bexp.getLeftOperand();
        this.leftOperand = new XmlBasicLeftOperand();

        if (lO instanceof BasicField) {
            this.leftOperand.setBasicField(lO.toString());
        } else if (lO instanceof BasicExpression) {
            this.leftOperand.setBasicExpression(new XmlFilterBasicExpression((BasicExpression) lO));
        }

        this.operator = bexp.getOperator().toString();

        this.rightOperand = new XmlBasicRightOperand();
        Object rO = bexp.getRightOperand();

        if (rO instanceof BasicExpression) {
            this.rightOperand.setBasicExpression(new XmlFilterBasicExpression((BasicExpression) rO));
        } else if (rO instanceof SearchValue) {
            this.rightOperand.setSearchValue(new XmlFilterSearchValue((SearchValue) rO));
        } else {
            this.rightOperand.setValue(rO);
        }

    }

    public XmlBasicLeftOperand getLeftOperand() {
        return this.leftOperand;
    }

    public void setLeftOperand(XmlBasicLeftOperand value) {
        this.leftOperand = value;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String value) {
        this.operator = value;
    }

    public XmlBasicRightOperand getRightOperand() {
        return this.rightOperand;
    }

    public void setRightOperand(XmlBasicRightOperand value) {
        this.rightOperand = value;
    }

    public BasicExpression getBasicExpression() {
        return new BasicExpression(this.leftOperand.getBasicValue(), new BasicOperator(this.operator),
                this.rightOperand.getBasicValue());
    }

}
