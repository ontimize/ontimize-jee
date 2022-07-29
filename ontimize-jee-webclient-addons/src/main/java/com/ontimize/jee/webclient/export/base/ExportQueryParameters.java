package com.ontimize.jee.webclient.export.base;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ontimize.jee.server.rest.FilterParameter;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = ExcelExportQueryParameters.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ExcelExportQueryParameters.class, name = "xlsx"),
    @JsonSubTypes.Type(value = BaseExportQueryParameters.class, name = "csv")
//TODO    @JsonSubTypes.Type(value = PdfExportQueryParameters.class, name = "pdf")
})

public interface ExportQueryParameters {

  FilterParameter getQueryParam();
  
  void setQueryParam(FilterParameter filterParameter);

  String getDao();
  
  String getService();

  
  String getPath();
  
  public boolean isAdvQuery();

}
