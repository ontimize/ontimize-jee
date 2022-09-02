package com.ontimize.jee.webclient.export.style;

import java.text.Format;

public interface PdfDataFormat {

  Format getFormat(String pattern, Class<?> clazz);
}
