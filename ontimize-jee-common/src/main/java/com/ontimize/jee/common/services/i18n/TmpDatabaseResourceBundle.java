package com.ontimize.jee.common.services.i18n;

import java.util.Map;
import java.util.Locale;

import com.ontimize.gui.i18n.DatabaseResourceBundle;

/**
 * The Class TmpDatabaseResourceBundle. Ontimize llama a la variable del Locale "locale" igual que
 * se llama en la clase ResourceBundle, y eso no le gusta nada a Hessian.
 */
public class TmpDatabaseResourceBundle extends DatabaseResourceBundle {

    /** The mylocale. */
    protected Locale mylocale;

    /**
     * The Constructor.
     * @param data the data
     * @param l the l
     */
    public TmpDatabaseResourceBundle(Map data, Locale l) {
        super(data, l);
        this.mylocale = l;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.i18n.DatabaseResourceBundle#getLocale()
     */
    @Override
    public Locale getLocale() {
        return this.mylocale;
    }

}
