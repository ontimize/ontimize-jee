/**
 * CustomHessianURLConnectionFactory.java 18-abr-2013
 *
 *
 *
 */
package com.ontimize.jee.common.hessian;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.client.AbstractHessianConnectionFactory;
import com.caucho.hessian.client.HessianConnection;
import com.ontimize.jee.common.session.HeaderAttribute;
import com.ontimize.jee.common.session.HeaderAttributesProvider;
import com.ontimize.jee.common.tools.SafeCasting;

/**
 * URLConnectionFactory personalizada para conectar con hessian. Tamién envía el locale del
 * usuario en las peticiones.
 *
 * @author <a href="user@email.com">Author</a> Use apache httpclient version
 */
@Deprecated
public class OntimizeHessianURLConnectionFactory extends AbstractHessianConnectionFactory {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(OntimizeHessianURLConnectionFactory.class.getName());

    private HeaderAttributesProvider headerAttributesProvider;

    /**
     * Opens a new or recycled connection to the HTTP server.
     * @param url the url
     * @return the hessian connection
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public HessianConnection open(final URI url) throws IOException {
        // Prepare authentication

        OntimizeHessianURLConnectionFactory.logger.trace(this + " open(" + url + ")");

        URLConnection conn = url.toURL().openConnection();

        long connectTimeout = this.getHessianProxyFactory().getConnectTimeout();

        if (connectTimeout >= 0) {
            conn.setConnectTimeout(SafeCasting.longToInt(connectTimeout));
        }

        conn.setDoOutput(true);
        if (conn instanceof HttpURLConnection) {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setUseCaches(false);
        }

        long readTimeout = this.getHessianProxyFactory().getReadTimeout();

        if (readTimeout > 0) {
            try {
                conn.setReadTimeout(SafeCasting.longToInt(readTimeout));
            } catch (Exception e) {
                OntimizeHessianURLConnectionFactory.logger.trace(null, e);
            }
        }
        conn.setRequestProperty("Content-Type", "x-application/hessian");

        this.addCustomHeaders(conn);

        return new OntimizeHessianURLConnection(url, conn);
    }

    /**
     * Adds custom headers to the request.
     * @param connection the hessian connection
     */
    protected void addCustomHeaders(URLConnection connection) {
        Locale locale = Locale.getDefault();
        HeaderAttribute ha = new HeaderAttribute();
        ha.setName("locale-country");
        ha.setValue(locale.getCountry());
        HeaderAttribute hb = new HeaderAttribute();
        hb.setName("locale-language");
        hb.setValue(locale.getLanguage());
        List<HeaderAttribute> has = new ArrayList<>();
        has.add(ha);
        has.add(hb);
        Collection<HeaderAttribute> headerAttributes;
        if (this.headerAttributesProvider != null) {
            headerAttributes = this.headerAttributesProvider.getHeaderAttributes(has);
        } else {
            headerAttributes = has;
        }
        for (HeaderAttribute headerAtt : headerAttributes) {
            if (headerAtt.getName() != null) {
                connection.setRequestProperty(headerAtt.getName(), headerAtt.getValue());
            }
        }
    }

    public void setHeaderAttributesProvider(HeaderAttributesProvider headerAttributesProvider) {
        this.headerAttributesProvider = headerAttributesProvider;
    }

}
