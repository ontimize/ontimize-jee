package com.ontimize.jee.desktopclient.locator.security;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.gui.container.Row;
import com.ontimize.jee.common.tools.ObjectWrapper;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class LoginIntoOAuth2Component extends Row {

    private static final Logger logger = LoggerFactory.getLogger(LoginIntoOAuth2Component.class);

    private WebView webView;

    private JFXPanel fxPanel;

    /**
     * Instantiates a new ExpressionDataField.
     * @param parameters the parameters
     */
    public LoginIntoOAuth2Component(Hashtable parameters) {
        super(parameters);
        this.createFxPanel();
        this.setOpaque(true);
        this.setBackground(Color.yellow);
    }

    private void createFxPanel() {
        this.fxPanel = new JFXPanel();
        this.fxPanel.setOpaque(true);
        this.fxPanel.setBackground(Color.red);
        this.setLayout(new BorderLayout());
        this.add(this.fxPanel, BorderLayout.CENTER);
        synchronized (this.fxPanel) {

            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    LoginIntoOAuth2Component.this.fxPanel.setScene(LoginIntoOAuth2Component.this.createBasicScene());
                    synchronized (LoginIntoOAuth2Component.this.fxPanel) {
                        LoginIntoOAuth2Component.this.fxPanel.notify();
                    }
                }
            });
            try {
                this.fxPanel.wait();
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private Scene createBasicScene() {
        this.webView = new WebView();
        this.webView.setId("webViewPanel");

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(this.webView);
        return new Scene(borderPane, 10, 10);
    }

    @Override
    public Object getConstraints(LayoutManager parentLayout) {
        Object constraints = super.getConstraints(parentLayout);
        if (constraints instanceof GridBagConstraints) {
            return new GridBagConstraints(-1, -1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0);
        }
        return constraints;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.webView != null) {
            this.webView.setDisable(!enabled);
        }
    }

    public void showLoginScreen(final String url, final ObjectWrapper<String> wrapper) {
        try {
            final String redirect = LoginIntoOAuth2Component.splitQuery(new URI(url)).get("redirect_uri");
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    LoginIntoOAuth2Component.this.webView.getEngine().load(url);
                }
            });

            if (redirect != null) {
                this.webView.getEngine().locationProperty().addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue,
                            String newValue) {
                        if ((newValue != null) && newValue.startsWith(redirect)) {
                            if (wrapper != null) {
                                LoginIntoOAuth2Component.this.webView.getEngine().getLoadWorker().cancel();
                                wrapper.setValue(newValue);
                            }
                        }
                    }
                });
            }

        } catch (Exception ex) {
            LoginIntoOAuth2Component.logger.error("Error loading login screen", ex);
        }
    }

    public static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

}
