package com.ontimize.jee.desktopclient.test.session;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * A simple example that uses HttpClient to execute an HTTP request against a target site that
 * requires user authentication.
 */
public class ClientAuthentication {

    public static void main(String[] args) throws Exception {

        SocketConfig config = SocketConfig.custom().setSoKeepAlive(true).setSoTimeout(5000).build();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        CloseableHttpClient httpclient = HttpClients.custom()
            .disableAutomaticRetries()
            .disableAuthCaching()
            .setDefaultCredentialsProvider(credentialsProvider)
            .setDefaultSocketConfig(config)
            .build();

        credentialsProvider.setCredentials(new AuthScope("localhost", 9999),
                new UsernamePasswordCredentials("username", "password"));

        HttpGet httpget = new HttpGet("http://localhost:9999/rexunta/services/hessian");

        System.out.println("executing request" + httpget.getRequestLine());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());
        if (entity != null) {
            System.out.println("Response content length: " + entity.getContentLength());
        }
        if (entity != null) {
            entity.consumeContent();
        }

        // When HttpClient instance is no longer needed,
        // shut down the connection manager to ensure
        // immediate deallocation of all system resources
        httpclient.getConnectionManager().shutdown();
    }

}
