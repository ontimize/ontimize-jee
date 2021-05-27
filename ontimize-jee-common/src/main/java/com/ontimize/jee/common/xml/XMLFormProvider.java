package com.ontimize.jee.common.xml;

public interface XMLFormProvider extends java.rmi.Remote {

    public String getXMLForm(String form, int userid) throws Exception;

}
