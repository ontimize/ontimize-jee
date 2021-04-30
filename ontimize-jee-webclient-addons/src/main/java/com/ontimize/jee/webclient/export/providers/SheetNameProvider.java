package com.ontimize.jee.webclient.export.providers;

import com.ontimize.jee.webclient.export.SheetContext;

import javafx.util.Callback;

/**
 * Provider donde el usuario puede decidir si desea una hoja nueva. Cada vez que se exporta una fila
 * se llama a getSheetName con el contexto de la hoja. Si devuelve el nombre de la hoja actual, se
 * continúa con la misma. Si devuelve un nombre diferente, si no existe una hoja con ese nombre se
 * crea y se convierte en la hoja actual.
 *
 * @author <a href="antonio.vazquez@imatia.com">antoniova</a>
 */
public interface SheetNameProvider {

    // Nombre de la primera o única hoja del libro
    String getDefaultSheetName();

    // Nombre de las posibles subsiguientes hojas
    Callback<SheetContext, String> getSheetName();

}
