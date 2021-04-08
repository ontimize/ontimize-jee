package com.ontimize.jee.desktopclient.locator.security;

import java.util.Hashtable;

import javax.swing.JDialog;

import com.ontimize.jee.common.tools.ObjectWrapper;
import com.ontimize.jee.desktopclient.components.WindowTools;

public class LoginIntoOAuth2Dialog extends JDialog {

    private final LoginIntoOAuth2Component component;

    public LoginIntoOAuth2Dialog() {
        super(WindowTools.getActiveWindow());
        this.setTitle("Oauth2");
        this.component = new LoginIntoOAuth2Component(new Hashtable<>());
        this.getContentPane().add(this.component);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
    }

    public void showLoginScreen(String url, ObjectWrapper<String> redirectUrl) {
        this.component.showLoginScreen(url, redirectUrl);
        this.setVisible(true);
    }

}
