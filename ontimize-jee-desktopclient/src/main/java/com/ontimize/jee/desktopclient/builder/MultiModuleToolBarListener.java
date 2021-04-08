package com.ontimize.jee.desktopclient.builder;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import com.ontimize.gui.DefaultToolBarListener;

/**
 * Se encarga de repartir los eventos a los escuchadores de menu de los diferentes módulos
 * configurados en la aplicación.
 *
 * @see MultiModuleMenuEvent
 */
public class MultiModuleToolBarListener extends DefaultToolBarListener {

    /** The listener list. */
    protected List<IModuleActionToolBarListener> listenerList;

    /**
     * Instantiates a new multi module menu listener.
     */
    public MultiModuleToolBarListener() {
        super();
        this.listenerList = new ArrayList<>();
    }

    /**
     * Adds the listener.
     * @param listener the listener
     */
    public void addListener(IModuleActionToolBarListener listener) {
        this.listenerList.add(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ontimize.gui.DefaultActionMenuListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        for (IModuleActionToolBarListener listener : this.listenerList) {
            if (listener.actionPerformed(e)) {
                return;
            }
        }
        super.actionPerformed(e);
    }

}
