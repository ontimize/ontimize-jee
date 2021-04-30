package com.ontimize.jee.desktopclient.components;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SwingUtils.
 */
public class SwingTools {

    private static final Logger logger = LoggerFactory.getLogger(SwingTools.class);

    /**
     * Invoke in ed th.
     * @param runnable the runnable
     */
    public static void invokeInEDTh(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception exception) {
                SwingTools.logger.error("SwingUtils.invokeInEDTh: Exception using InvokeAndWait.", exception);
            }
        }
    }

    public static void invokeInEDThLater(Runnable runnable) {
        SwingUtilities.invokeLater(runnable);
    }

}
