package com.ontimize.jee.webclient.excelexport;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author <a href="antonio.vazquez@imatia.com">antonio.vazquez</a>
 */

public class ExcelExportParameter extends BaseExcelExportParameters implements Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement
	private List<Map<Object, Object>> data;

	public ExcelExportParameter() {
		super();
	}
	
	public ExcelExportParameter(List<Map<Object, Object>> data) {
		super();
		this.data = data;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public List<Map<Object, Object>> getData() {
		return data;
	}

	public void setData(List<Map<Object, Object>> data) {
		this.data = data;
	}
}
