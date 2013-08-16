package com.evry.websocket.push;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.websocket.Session;

public class ServerUpdate implements Callable<Boolean> {

	private Set<Session> peers;
	private int value;
	private boolean run = true;

	public ServerUpdate(Set<Session> peers) {
		this.peers = peers;
	}

	public Boolean call() throws IOException {
		while (run) {
			value = (int) (Math.random() * 1000);
			for (Session peer : peers) {				
				peer.getBasicRemote().sendText(String.valueOf(value));
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
				return false;
			}
		}
		return true;
	}

	public int getValue() {
		return value;
	}
}
