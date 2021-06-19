package mutoserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;

public class ConnectionHandler extends Thread {
	private Socket soc;
	private String ID;

	public ConnectionHandler(Socket soc, String ID) {
		this.soc = soc;
		this.ID = ID;
		start();
	}

	public void close() throws IOException {
		System.out.println("CONNECTIONHANDLER: THREAD FOR '" + soc.getInetAddress().getHostAddress() + ":"
				+ soc.getPort() + "' CLOSING...");
		soc.close();
		Server.eliminate(this);
		interrupt();
	}

	public String getID() {
		return ID;
	}

	public Socket getSocket() {
		return soc;
	}

	public void run() {
		System.out.println("Connection handled: '" + soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "'");

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream(), "UTF8"));

			System.out.println(
					soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "::" + "Sending verification");

			out.append("MUTOSERVER-VERIFY-").append("\n");
			out.flush();

			long timeBegin = System.currentTimeMillis();

			BufferedReader read = new BufferedReader(new InputStreamReader(soc.getInputStream(), "UTF8"));

			while (true) {
				System.out.println(soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "::"
						+ "Waiting for client response");
				String result = read.readLine();
				long deltaT = System.currentTimeMillis() - timeBegin;
				// result must be returned within 5 seconds.
				if (result.equals("MUTOCLIENT-RETURN-") && deltaT < 5000L) {
					System.out.println(
							soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "::" + "Client verified.");
					break;
				} else {
					System.out.println(soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "::"
							+ "Failed to verify client... closing thread.");
					close();
				}
			}
			// read.close();
			// out.close();
			try {
				proceed();
			} catch (SocketException e) {
				System.err.println("ALERT: Connection closed.");
			} catch (NullPointerException e) {
				close();
				System.err.println(
						"Connection closed: '" + soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "'");
				System.err.println("ALERT: Connection closed.");

			}

		} catch (IOException e) {
			System.err.println(
					"Fatal error for connection '" + soc.getInetAddress().getHostAddress() + ":" + soc.getPort() + "'");
			e.printStackTrace();
			try {
				close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	private void proceed() throws UnsupportedEncodingException, IOException, NullPointerException {
		System.out.println("YAY IT WORKED!!!");
		InputStreamReader isr = new InputStreamReader(soc.getInputStream(), "UTF-8");
		BufferedReader in = new BufferedReader(isr);
		OutputStreamWriter osw = new OutputStreamWriter(soc.getOutputStream(), "UTF-8");
		BufferedWriter out = new BufferedWriter(osw);

		while (true) {
			String read = in.readLine();
			if (read.equalsIgnoreCase("ping")) {
				out.append("pong").append("\n");
				out.flush();
			}
		}

		/*
		 * isr.close(); in.close(); osw.close(); out.close();
		 */
	}
}
