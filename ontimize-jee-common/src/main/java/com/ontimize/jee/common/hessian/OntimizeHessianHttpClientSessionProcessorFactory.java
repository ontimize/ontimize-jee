package com.ontimize.jee.common.hessian;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.client.protocol.RequestProxyAuthentication;
import org.apache.http.client.protocol.RequestTargetAuthentication;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.config.SocketConfig;
import org.apache.http.config.SocketConfig.Builder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.jee.common.tools.ParseUtilsExtended;
import com.ontimize.jee.common.tools.SafeCasting;
import com.ontimize.util.Base64Utils;

/**
 * A factory for creating OntimizeHessianHttpClientSessionProcessor objects.
 */
public final class OntimizeHessianHttpClientSessionProcessorFactory {

	private static final Logger					logger				= LoggerFactory.getLogger(OntimizeHessianHttpClientSessionProcessorFactory.class);
	public final static String					JWT_HEADER			= "X-Auth-Token";

	/** The sessionid. */
	// private static String SESSIONID;
	private static final CookieStore			httpCookieStore		= new BasicCookieStore();
	/** The request interceptor. */
	private static HttpRequestInterceptor		requestInterceptor	= new SessionIdHttpRequestInterceptor();
	/** The response interceptor. */
	private static HttpResponseInterceptor		responseInterceptor	= new SessionIdHttpResponseInterceptor();

	public static boolean						ENCRYPT				= true;
	public static String						JWT_TOKEN			= null;
	/** The httpproc. */
	private static HttpProcessor				httpproc			= HttpProcessorBuilder.create().add(new RequestAddCookies()).add(new ResponseProcessCookies())
			.add(OntimizeHessianHttpClientSessionProcessorFactory.requestInterceptor).add(OntimizeHessianHttpClientSessionProcessorFactory.responseInterceptor)
			.add(new RequestDefaultHeaders())
			// Required protocol interceptors
			.add(new RequestContent()).add(new RequestTargetHost())
			// Recommended protocol interceptors
			.add(new RequestClientConnControl()).add(new RequestUserAgent()).add(new RequestExpectContinue(false))
			// HTTP state management interceptors
			// HTTP authentication interceptors
			// httpproc.addInterceptor(new RequestAuthCache());
			.add(new RequestTargetAuthentication()).add(new RequestProxyAuthentication()).build();

	private static Map<AuthScope, Credentials>	credentials			= new HashMap<>();

	public static HttpProcessor getHttpProcessor() {
		return OntimizeHessianHttpClientSessionProcessorFactory.httpproc;
	}

	public static void setHttpProcessor(HttpProcessor httpProc) {
		OntimizeHessianHttpClientSessionProcessorFactory.httpproc = httpProc;
	}

	/**
	 * Gets the http processor.
	 *
	 * @return the http processor
	 */
	public static void addCredentials(URI uri, String userName, String password) {
		String host = uri.getHost();
		Integer port = uri.getPort();
		if (OntimizeHessianHttpClientSessionProcessorFactory.ENCRYPT) {
			OntimizeHessianHttpClientSessionProcessorFactory.credentials.put(new AuthScope(host, port, AuthScope.ANY_REALM, AuthSchemes.BASIC),
					new UsernamePasswordCredentials(userName, password));
			OntimizeHessianHttpClientSessionProcessorFactory.credentials.put(new AuthScope(host, port, AuthScope.ANY_REALM, AuthSchemes.DIGEST),
					new UsernamePasswordCredentials(userName, OntimizeHessianHttpClientSessionProcessorFactory.encrypt(password)));
		} else {
			OntimizeHessianHttpClientSessionProcessorFactory.credentials.put(new AuthScope(host, port), new UsernamePasswordCredentials(userName, password));
		}
	}

	public static boolean removeCredentials(URI uri) {
		String host = uri.getHost();
		Integer port = uri.getPort();
		Credentials remove = OntimizeHessianHttpClientSessionProcessorFactory.credentials.remove(new AuthScope(host, port));
		return remove != null;
	}

	/**
	 * The Class SessionIdHttpRequestInterceptor.
	 */
	public static class SessionIdHttpRequestInterceptor implements HttpRequestInterceptor {

		/*
		 * (non-Javadoc)
		 * @see org.apache.http.HttpRequestInterceptor#process(org.apache.http.HttpRequest, org.apache.http.protocol.HttpContext)
		 */
		@Override
		public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
			if (!OntimizeHessianHttpClientSessionProcessorFactory.credentials.isEmpty()) {
				// CredentialsProvider credentialsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
				CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				for (Entry<AuthScope, Credentials> entry : OntimizeHessianHttpClientSessionProcessorFactory.credentials.entrySet()) {
					credentialsProvider.setCredentials(entry.getKey(), entry.getValue());
				}
				context.setAttribute(HttpClientContext.CREDS_PROVIDER, credentialsProvider);
			}
			if ((OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN != null) && (OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN.length() > 0)) {
				request.addHeader("Authorization", "Bearer " + OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN);
			}
		}
	}

	/**
	 * The Class SessionIdHttpResponseInterceptor.
	 */
	public static class SessionIdHttpResponseInterceptor implements HttpResponseInterceptor {

		/*
		 * (non-Javadoc)
		 * @see org.apache.http.HttpResponseInterceptor#process(org.apache.http.HttpResponse, org.apache.http.protocol.HttpContext)
		 */
		@Override
		public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
			Header jwtHeader = response.getFirstHeader(OntimizeHessianHttpClientSessionProcessorFactory.JWT_HEADER);
			if ((jwtHeader != null) && (jwtHeader.getValue() != null) && (jwtHeader.getValue().length() > 0)) {
				OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN = jwtHeader.getValue();
			} else if ((response.getStatusLine().getStatusCode() == 401) || (response.getStatusLine().getStatusCode() == 302)) {
				OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN = null;
			}
		}
	}

	// public static String getSESSIONID() {
	// return OntimizeHessianHttpClientSessionProcessorFactory.SESSIONID;
	// }

	static class SessionProcessorKey {

		private final String	key;
		private final Integer	port;

		public SessionProcessorKey(String key, Integer port) {
			super();
			this.key = key;
			this.port = port;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((this.key == null) ? 0 : this.key.hashCode());
			result = (prime * result) + ((this.port == null) ? 0 : this.port.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (this.getClass() != obj.getClass()) {
				return false;
			}
			SessionProcessorKey other = (SessionProcessorKey) obj;
			if (this.key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!this.key.equals(other.key)) {
				return false;
			}
			if (this.port == null) {
				if (other.port != null) {
					return false;
				}
			} else if (!this.port.equals(other.port)) {
				return false;
			}
			return true;
		}
	}

	private static String encrypt(String password) {
		try {
			MessageDigest md = java.security.MessageDigest.getInstance("SHA");
			// Get the password byes
			byte[] bytes = password.getBytes();
			md.update(bytes);
			byte[] ecriptedBytes = md.digest();

			char[] characters = Base64Utils.encode(ecriptedBytes);
			String result = new String(characters);
			return result;
		} catch (Exception e) {
			OntimizeHessianHttpClientSessionProcessorFactory.logger.error(null, e);
			return null;
		}
	}

	public static CookieStore getCookieStore() {
		return OntimizeHessianHttpClientSessionProcessorFactory.httpCookieStore;
	}

	public static CloseableHttpClient createClient(long connectTimeout) {
		Builder socketConfigBuilder = SocketConfig.custom().setSoKeepAlive(true);
		if (connectTimeout >= 0) {
			socketConfigBuilder.setSoTimeout(SafeCasting.longToInt(connectTimeout));
		}
		SocketConfig config = socketConfigBuilder.build();
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

		HttpClientBuilder clientBuilder = HttpClients.custom().disableAutomaticRetries().disableAuthCaching().setDefaultCredentialsProvider(credentialsProvider)
				.setDefaultSocketConfig(config).setHttpProcessor(OntimizeHessianHttpClientSessionProcessorFactory.getHttpProcessor())
				.setDefaultCookieStore(OntimizeHessianHttpClientSessionProcessorFactory.getCookieStore()).disableRedirectHandling().setMaxConnPerRoute(20);
		OntimizeHessianHttpClientSessionProcessorFactory.checkIgnoreSSLCerts(clientBuilder);
		CloseableHttpClient client = clientBuilder.build();

		return client;
	}

	private static void checkIgnoreSSLCerts(HttpClientBuilder clientBuilder) {
		boolean ignoreSSLCerts = ParseUtilsExtended.getBoolean(System.getProperty("ignoreSSLCerts"), false);
		if (ignoreSSLCerts) {
			SSLConnectionSocketFactory sslsf = null;
			try {
				SSLContextBuilder sshbuilder = new SSLContextBuilder();
				sshbuilder.loadTrustMaterial(new TrustStrategy() {

					@Override
					public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
						return true;
					}
				});
				sslsf = new SSLConnectionSocketFactory(sshbuilder.build(), NoopHostnameVerifier.INSTANCE);
				clientBuilder.setSSLSocketFactory(sslsf);
			} catch (Exception error) {
				OntimizeHessianHttpClientSessionProcessorFactory.logger.error(null, error);
			}
		}
	}

	public static Object getSESSIONID() {
		for (Cookie cookie : OntimizeHessianHttpClientSessionProcessorFactory.httpCookieStore.getCookies()) {
			if ("session".equals(cookie.getName().toLowerCase())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public static String getJwtToken() {
		return OntimizeHessianHttpClientSessionProcessorFactory.JWT_TOKEN;
	}
}
