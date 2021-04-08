package com.ontimize.jee.desktopclient.components;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.ontimize.gui.TipScroll;

/**
 * Utilidades de ventanas.
 */
public class WindowTools {

    /**
     * Instantiates a new window utils.
     */
    private WindowTools() {
        super();
    }

    /**
     * Localiza la ventana activa de la aplicacion, sino la encuentra devuelve la ultima que se creo.
     * @return the active window
     */
    public static Window getActiveWindow() {
        Window[] windows = Window.getWindows();
        Window w = null;
        for (Window window : windows) {
            if (window.isActive()) {
                w = window;
                break;
            }
        }
        List<Window> visibleWindows = new ArrayList<>();
        for (Window window : windows) {
            if (window.isVisible()) {
                visibleWindows.add(window);
            }
        }

        if ((w == null) && (visibleWindows.size() > 0)) {
            w = visibleWindows.get(visibleWindows.size() - 1);
        }
        while (w instanceof TipScroll) {
            w = w.getOwner();
        }
        return w;
    }

    /**
     * Gets the window ancestor.
     * @param component the component
     * @return the window ancestor
     */
    public static Window getWindowAncestor(Component component) {
        if (component == null) {
            return null;
        }
        Window windowAncestor = SwingUtilities.getWindowAncestor(component);
        if ((windowAncestor == null) && (component instanceof Window)) {
            windowAncestor = (Window) component;
        }
        return windowAncestor;
    }

    public static Rectangle getScreenBounds(Window wnd) {
        Rectangle sb;
        Insets si = WindowTools.getScreenInsets(wnd);

        if (wnd == null) {
            sb = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getBounds();
        } else {
            sb = wnd.getGraphicsConfiguration().getBounds();
        }

        sb.x += si.left;
        sb.y += si.top;
        sb.width -= si.left + si.right;
        sb.height -= si.top + si.bottom;
        return sb;
    }

    public static Insets getScreenInsets(Window wnd) {
        Insets si;

        if (wnd == null) {
            si = Toolkit.getDefaultToolkit()
                .getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration());
        } else {
            si = wnd.getToolkit().getScreenInsets(wnd.getGraphicsConfiguration());
        }
        return si;
    }

}
