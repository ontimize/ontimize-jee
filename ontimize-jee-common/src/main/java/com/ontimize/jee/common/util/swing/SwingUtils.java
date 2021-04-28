package com.ontimize.jee.common.util.swing;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.SwingUtilities;


public class SwingUtils {

    public static Window getWindowAncestor(Component source) {
        Window w = SwingUtilities.getWindowAncestor(source);
        if (w instanceof javax.swing.JWindow) {
            w = SwingUtils.getWindowAncestor(w.getParent());
        }
        return w;
    }

    /**
     * Centers the object in the screen. Useful when displaying new windows.
     * @param window
     */
    public static void center(Window window, Frame root) {
        int x = 0;
        int y = 0;

        Rectangle bounds = null;

        Window parentWindow = null;
        if (window.getParent() instanceof Window) {
            parentWindow = (Window) window.getParent();
        } else {
            parentWindow = window;
        }

        if ((parentWindow == null) || (root == null) || window.equals(root)) {
            bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getBounds();
        } else {
            GraphicsEnvironment graphicsEnviroment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] graphicsDevice = graphicsEnviroment.getScreenDevices();
            // Find main application bounds.
            Rectangle parentBounds = parentWindow.getBounds();
            Point centerPoint = new Point(parentBounds.x + (parentBounds.width / 2),
                    parentBounds.y + (parentBounds.height / 2));
            for (int i = 0; i < graphicsDevice.length; i++) {
                Rectangle currentBounds = graphicsDevice[i].getDefaultConfiguration().getBounds();
                if (currentBounds.contains(centerPoint)) {
                    bounds = currentBounds;
                    break;
                }
            }
        }

        if (bounds == null) {
            bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getBounds();
        }

        x = (bounds.width / 2) - (window.getWidth() / 2);
        y = (bounds.height / 2) - (window.getHeight() / 2);

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (x > bounds.width) {
            x = 0;
        }
        if (y > bounds.height) {
            y = 0;
        }
        window.setLocation(bounds.x + x, bounds.y + y);
    }

}
