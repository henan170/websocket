package com.evry.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.evry.websocket.push.ServerUpdate;

@ServerEndpoint("/push")
public class PushEndpoint {

	private static final Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	private static final ServerUpdate serverUpdate = new ServerUpdate(peers);

	static {
		executorService.submit(serverUpdate);
	}

	@OnOpen
	public void onOpen(Session peer) throws IOException {
		System.out.println("onOpen push");
		peers.add(peer);
		peer.getBasicRemote().sendText(String.valueOf(serverUpdate.getValue()));
	}

	@OnClose
	public void onClose(Session peer) {
		peers.remove(peer);
	}

	@OnMessage
	public void onMessage(String message, Session client) throws IOException, EncodeException {
		for (Session peer : peers) {
			peer.getBasicRemote().sendObject(message);
		}
	}

}
