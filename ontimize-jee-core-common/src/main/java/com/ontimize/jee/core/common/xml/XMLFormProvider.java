package com.ontimize.jee.core.common.xml;

public interface XMLFormProvider extends java.rmi.Remote {

    public String getXMLForm(String form, int userid) throws Exception;

}
