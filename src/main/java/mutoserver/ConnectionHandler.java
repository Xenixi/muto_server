package mutoserver;

import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler extends Thread {
	private Socket soc;
	private String ID;
	public ConnectionHandler(Socket soc, String ID) {
		this.soc = soc;
		this.ID = ID;
		start();
	}
	public void close() throws IOException {
		soc.close();
		interrupt();
	}
	public String getID() {
		return ID;
	}
	public Socket getSocket() {
		return soc;
	}
	public void run() {
		System.out.println("Connection handled: '"  + soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "'");
		
	}
}
