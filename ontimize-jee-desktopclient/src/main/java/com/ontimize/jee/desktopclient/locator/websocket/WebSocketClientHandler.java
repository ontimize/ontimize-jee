package com.ontimize.jee.desktopclient.locator.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

import javax.websocket.EncodeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.ontimize.jee.common.exceptions.OntimizeJEEException;
import com.ontimize.jee.common.hessian.OntimizeHessianHttpClientSessionProcessorFactory;
import com.ontimize.jee.common.hessian.OntimizeHessianProxyFactoryBean;
import com.ontimize.jee.common.services.user.IUserInformationService;
import com.ontimize.jee.common.websocket.WebsocketWrapperMessage;
import com.ontimize.jee.desktopclient.spring.BeansFactory;

public class WebSocketClientHandler extends TextWebSocketHandler implements IWebSocketClientHandler, InitializingBean {
	private static final Logger					logger		= LoggerFactory.getLogger(WebSocketClientHandler.class);

	private WebSocketSession					webSocketSession;
	private String								webSocketRelativeUrl;
	private String								webSocketUrl;
	private boolean								connected;
	private final List<IWebSocketEventListener>	listeners;
	private final ThreadPoolExecutor			executor	= (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	public WebSocketClientHandler() {
		super();
		this.listeners = new ArrayList<>();
		this.connected = false;
		this.reconnectInThread();
	}

	public void setWebSocketRelativeUrl(String url) {
		this.webSocketRelativeUrl = url;
	}

	public String getWebSocketRelativeUrl() {
		return this.webSocketRelativeUrl;
	}

	public String getWebSocketUrl() {
		return this.webSocketUrl;
	}

	public void setWebSocketUrl(String webSocketUrl) {
		this.webSocketUrl = webSocketUrl;
	}

	@Override
	public void afterPropertiesSet() {
		if ((this.getWebSocketRelativeUrl() != null) && (this.getWebSocketRelativeUrl().length() > 0)) {
			String base = System.getProperty(OntimizeHessianProxyFactoryBean.SERVICES_BASE_URL);
			if (base != null) {
				if ((base.charAt(base.length() - 1) != '/') && (this.getWebSocketRelativeUrl().charAt(0) != '/')) {
					base = base + '/';
				}
				this.setWebSocketUrl(base + this.getWebSocketRelativeUrl());
			}
		}
	}

	@Override
	public void addWebSocketEventListener(IWebSocketEventListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeWebSocketEventListener(IWebSocketEventListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * Connect to WebSocket
	 *
	 * @param sServer
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 * @throws TimeoutException
	 */
	protected void connect() throws InterruptedException, ExecutionException, URISyntaxException, TimeoutException {
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(OntimizeHessianHttpClientSessionProcessorFactory.createClient(-1));
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		List<Transport> transports = new ArrayList<>(2);
		transports.add(new WebSocketTransport(new StandardWebSocketClient()));
		transports.add(new RestTemplateXhrTransport(restTemplate));

		SockJsClient sockJsClient = new SockJsClient(transports);
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

		ListenableFuture<WebSocketSession> handshake = sockJsClient.doHandshake(this, headers, new URI(this.getWebSocketUrl()));
		sockJsClient.start();
		this.webSocketSession = handshake.get();
		WebSocketClientHandler.logger.info("websocket connecting");
	}

	/**
	 * Send message to server by WebSocket
	 *
	 * @param message
	 * @throws OntimizeJEEException
	 * @throws IOException
	 * @throws EncodeException
	 */
	@Override
	public void sendMessage(Integer messageType, String messageSubtype, Object ob) throws OntimizeJEEException {
		try {
			TextMessage textMessage = new TextMessage(new WebsocketWrapperMessage(messageType, messageSubtype, ob).serialize());
			this.checkConnection();
			this.webSocketSession.sendMessage(textMessage);
		} catch (OntimizeJEEException ex) {
			throw ex;
		} catch (Exception error) {
			WebSocketClientHandler.logger.error(null, error);
			throw new OntimizeJEEException(error);
		}
	}

	@Override
	public void sendMessage(Integer messageType, Object ob) throws OntimizeJEEException {
		this.sendMessage(messageType, null, ob);
	}

	private void checkConnection() throws OntimizeJEEException {
		if (!this.connected) {
			throw new OntimizeJEEException("websocket not connected");
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		WebSocketClientHandler.logger.info("websocket connection established");
		this.connected = true;
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		WebSocketClientHandler.logger.info("websocket conneciton closed");
		this.connected = false;
		this.webSocketSession = null;
		this.reconnectInThread();
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		super.handleTransportError(session, exception);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
		super.handleTextMessage(session, textMessage);
		WebsocketWrapperMessage wrappedMessage = WebsocketWrapperMessage.deserialize(textMessage.getPayload());
		this.fireMessageEvent(wrappedMessage);
	}

	private void fireMessageEvent(WebsocketWrapperMessage wrappedMessage) {
		for (IWebSocketEventListener listener : this.listeners.toArray(new IWebSocketEventListener[0])) {
			listener.onWebSocketMessageReceived(wrappedMessage);
		}
	}

	private void reconnectInThread() {
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// do nothing
					}
					try {
						WebSocketClientHandler.this.connect();
						WebSocketClientHandler.logger.info("conectado");
						return;
					} catch (HttpClientErrorException ex) {
						if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
							// Necessary to reconnect, the sessionId is not valid anymore -> Do some operation
							WebSocketClientHandler.logger.error("INVALID_SESSION__MUST_BE_RE-LOGGED");
							BeansFactory.getBean(IUserInformationService.class).getUserInformation();
							WebSocketClientHandler.logger.error("RE-LOGGED_SUCCESSFULLY");
						} else {
							WebSocketClientHandler.logger.error(null, ex);
						}
					} catch (Exception error) {
						WebSocketClientHandler.logger.error(null, error);
					}
				}
			}
		});

	}
}