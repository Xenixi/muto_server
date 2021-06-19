package mutoserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.ini4j.Wini;

public class Server {

	static File configFile = new File("./server.ini");

	static Wini config;

	static List<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();

	static int port = 8657;

	// Removes connection handlers that are no longer needed (client offline).

	public static void eliminate(ConnectionHandler handler) {
		connections.remove(handler);

	}

	public static void main(String[] args) throws InterruptedException, IOException {
		// MUTO SERVER - HANDLES ALL CLIENT CONNECTIONS
		System.out.println("Muto Server launched - waiting 1.5s...");
		Thread.sleep(1500);
		System.out.println("Loading config...");

		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
			}
			config = new Wini(configFile);
		} catch (Exception e) {
			System.err.println("Error in main thread: 'loading config...'\nexiting...");
			System.exit(0);
		}

		// Configuration

		config.put("Muto-Server", "Version", 1.1);
		config.put("Muto-Server", "Server Launches", (config.fetch("Muto-Server", "Server Launches") == null ? 1
				: Integer.parseInt(config.fetch("Muto-Server", "Server Launches")) + 1));

		if (config.fetch("Server", "Port") == null) {
			config.put("Server", "Port", 8657);
		}
		if (config.fetch("Data", "Path") == null) {
			config.put("Data", "Path", new File("./data/").getAbsolutePath());
		}

		// Checks

		port = Integer.parseInt(config.fetch("Server", "Port"));
		config.store();

		System.out.println("Config loaded.\nChecking data...");
		File dataFolder = new File(config.fetch("Data", "Path"));

		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}

		System.out.println("Checked.");

		System.out.println("All startup tasks completed.");

		System.out.println("Starting command listener...");

		System.out.println("Starting client listen loop...");

		Thread listenThread = new Thread(new Runnable() {

			public void run() {
				System.out.println("Listener ready.");
				try {
					ServerSocket ssoc = new ServerSocket(port);

					while (true) {
						Socket soc = ssoc.accept();
						connections.add(new ConnectionHandler(soc,
								soc.getInetAddress().getHostAddress() + ":" + soc.getPort()));

					}
				} catch (IOException e) {
					System.err.println("ERROR GENERATED IN LISTENTHREAD");
					e.printStackTrace();
				}

			}
		});

		// listener thread (for all new connections)

		listenThread.start();

		System.out.println("Command input active. (help - 'h'):");

		Scanner scan = new Scanner(System.in);
		boolean confirmExit = false, confirmKickAll = false;

		while (true) {
			String cmd = scan.nextLine();

			if (confirmExit) {
				if (cmd.equalsIgnoreCase("x")) {
					System.out.println("Kicking all clients and exiting forcibly...");
					for (int i = 0; i < connections.size(); i++) {
						connections.get(i).close();
					}

					System.exit(0);
				} else {
					System.out.println("Operation cancelled.");
					confirmExit = false;
				}
			}
			if (confirmKickAll) {
				if (cmd.equalsIgnoreCase("fk")) {
					System.out.println("Kicking all clients...");
					for (int i = 0; i < connections.size(); i++) {
						connections.get(i).close();
					}
					confirmKickAll = false;
					cmd = "";
				} else {
					System.out.println("Operation cancelled.");
					confirmKickAll = false;
				}
			}

			if (cmd.equalsIgnoreCase("h")) {
				System.out.println("Help: ");
				System.out
						.println("help - h\nexit - x\nforcibly kick all clients - fk\nlist all connected clients - ls");
			} else if (cmd.equalsIgnoreCase("x")) {
				System.out.println("THIS WILL EXIT AND KICK ALL CLIENTS - Please confirm by typing 'x' again.");
				confirmExit = true;
			} else if (cmd.equalsIgnoreCase("fk")) {
				System.out.println("Please confirm that you would like to KICK ALL CLIENTS by typing 'fk' again.");
				confirmKickAll = true;
			} else if (cmd.equalsIgnoreCase("ls")) {
				System.out.println("Connected clients:");
				int i = 0;
				for (ConnectionHandler handler : connections) {
					System.out.println(i + ".) - " + handler.getID());
				}
			}
		}

	}
}
