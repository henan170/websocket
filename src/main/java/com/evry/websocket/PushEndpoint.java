package com.evry.websocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

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

	private static final ReentrantLock openLock = new ReentrantLock();
	private static final ReentrantLock closeLock = new ReentrantLock();
	private static ServerUpdate serverUpdate;

	@OnOpen
	public void onOpen(Session peer) throws IOException {

		if (peers.isEmpty()) {
			openLock.lock();
			try {
				if (peers.isEmpty()) {
					serverUpdate = new ServerUpdate(peers);
					executorService.submit(serverUpdate);
				}
			} finally {
				openLock.unlock();
			}
		}

		peers.add(peer);
		peer.getBasicRemote().sendText(String.valueOf(serverUpdate.getValue()));
	}

	@OnClose
	public void onClose(Session peer) {
		peers.remove(peer);

		if (peers.isEmpty()) {
			closeLock.lock();
			try {
				if (peers.isEmpty()) {
					serverUpdate.stop();
				}
			} finally {
				closeLock.unlock();
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session client) throws IOException, EncodeException {
		for (Session peer : peers) {
			peer.getBasicRemote().sendObject(message);
		}
	}

}
