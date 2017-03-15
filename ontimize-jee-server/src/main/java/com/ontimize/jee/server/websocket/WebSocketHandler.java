package com.ontimize.jee.server.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.ontimize.jee.common.websocket.WebsocketWrapperMessage;

/**
 * The Class WebSocketHandler.
 */
@Component("websocketHandler")
@Lazy(value = true)
public class WebSocketHandler extends TextWebSocketHandler implements IWebSocketHandler {

	/** The Constant logger. */
	private static final Logger					logger	= LoggerFactory.getLogger(WebSocketHandler.class);

	/** The sessions. */
	private final List<WebSocketSession>		sessions;
	private final List<IWebSocketEventListener>	messageListeners;

	/**
	 * Instantiates a new web socket handler.
	 */
	public WebSocketHandler() {
		super();
		this.sessions = new ArrayList<WebSocketSession>();
		this.messageListeners = new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.socket.handler.AbstractWebSocketHandler#handleTextMessage(org.springframework.web.socket.WebSocketSession,
	 * org.springframework.web.socket.TextMessage)
	 */
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		WebsocketWrapperMessage wrappedMessage = WebsocketWrapperMessage.deserialize(message.getPayload());
		this.fireMessageReceived(session, wrappedMessage);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.socket.handler.AbstractWebSocketHandler#afterConnectionEstablished(org.springframework.web.socket.WebSocketSession)
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		this.sessions.add(session);
		WebSocketHandler.logger.info("new websocket session from {}", session.getPrincipal().getName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.socket.handler.AbstractWebSocketHandler#afterConnectionClosed(org.springframework.web.socket.WebSocketSession,
	 * org.springframework.web.socket.CloseStatus)
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);
		this.sessions.remove(session);
		WebSocketHandler.logger.info("websocket session closed from {}", session.getPrincipal().getName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.socket.handler.AbstractWebSocketHandler#handleTransportError(org.springframework.web.socket.WebSocketSession, java.lang.Throwable)
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		super.handleTransportError(session, exception);
	}

	@Override
	public void addWebSocketEventListener(IWebSocketEventListener listener) {
		this.messageListeners.add(listener);
	}

	protected void fireMessageReceived(WebSocketSession session, WebsocketWrapperMessage message) {
		for (IWebSocketEventListener listener : this.messageListeners) {
			listener.onWebSocketMessageReceived(session, message);
		}
	}

	/**
	 * Send message.
	 *
	 * @param message
	 *            the message
	 * @throws IOException
	 */
	@Override
	public void sendMessage(Integer messageType, String messageSubtype, Object ob, WebSocketSession... receivers) {
		TextMessage textMessage = this.buildTextMessage(messageType, messageSubtype, ob);
		for (WebSocketSession session : receivers) {
			try {
				session.sendMessage(textMessage);
			} catch (IOException error) {
				// TODO deberia lanzar excepcion si falla con una session?
				WebSocketHandler.logger.error(null, error);
			}
		}
	}

	@Override
	public void sendMessage(Integer messageType, String messageSubtype, Object ob, WebSocketSession receiver) throws IOException {
		TextMessage textMessage = this.buildTextMessage(messageType, messageSubtype, ob);
		receiver.sendMessage(textMessage);
	}

	@Override
	public void sendBroadcastMessage(Integer messageType, String messageSubtype, Object ob) {
		TextMessage textMessage = this.buildTextMessage(messageType, messageSubtype, ob);
		for (WebSocketSession session : this.sessions) {
			try {
				session.sendMessage(textMessage);
			} catch (IOException error) {
				// TODO deberia lanzar excepcion si falla con una session?
				WebSocketHandler.logger.error(null, error);
			}
		}
	}

	private TextMessage buildTextMessage(Integer messageType, String messageSubtype, Object ob) {
		return new TextMessage(new WebsocketWrapperMessage(messageType, messageSubtype, ob).serialize());
	}

	@Override
	public List<WebSocketSession> getSessionsForUser(String userLogin) {
		List<WebSocketSession> res = new ArrayList<>();
		if (userLogin == null) {
			return res;
		}
		for (WebSocketSession session : this.sessions) {
			if (userLogin.equals(session.getPrincipal().getName())) {
				res.add(session);
			}
		}
		return res;
	}


}