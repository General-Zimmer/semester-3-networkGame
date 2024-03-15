package spil.version1.server;

import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;



public class Server {

	static final Queue<String> actions = new PriorityQueue<>();
	static GameLogic gameLogic = new GameLogic(false);
	static final PlayerConn[] playerConns = new PlayerConn[5];
	static final Queue<PlayerConn> joinQueue = new PriorityQueue<>();


	public static void main(String[] args) {
		new JoinThread().start();
		new gameTickThread().start();
		new ServerThread(playerConns, actions).start();
	}

	private static class JoinThread extends Thread {
		ServerSocket welcomeSocket;
		private JoinThread() {
			try {
				this.welcomeSocket = new ServerSocket(1337);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			while (true) {
				Socket connectionSocket;
				try {
					connectionSocket = welcomeSocket.accept();
					if (sizeOfConns() + joinQueue.size() >= playerConns.length) {
						connectionSocket.close();
					} else {
						connectionSocket.setSoTimeout(120000);
						ObjectOutputStream objectToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
						DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
						String name = read.readLine().split(" ")[2];

						PlayerConn playerConn = new PlayerConn(connectionSocket, outToClient, objectToClient, read, name);
						joinQueue.add(playerConn);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}


	private static class gameTickThread extends Thread {
		public void run() {
			double leftOver = 0;
			double msPerTick = 50;
			long LastTickID = 0;
			while (true) {
				double beforeTime = System.nanoTime();
				playerJoins();
				gameLogic.movePlayers(actions);
				if (!(gameLogic.getState().getTickID() == LastTickID)) {
					LastTickID = gameLogic.getState().getTickID();
					sendStateToAll();
				}

				double afterTime = System.nanoTime();
				double timeLeftonTick = msPerTick - (afterTime - beforeTime) / 1000000.0;


				if (timeLeftonTick < 0) { // Check for if server is running behind
					System.out.println("Server is running behind");
					System.out.println("   timeLeftonTick: " + timeLeftonTick);
					System.out.println("   leftOver: " + leftOver);
					leftOver += timeLeftonTick;
				} else if (leftOver > 0 && leftOver < msPerTick) { // Check for if left is bigger than a tick and not 0
					leftOver -= msPerTick; 							// (aka, the server is running behind and needs to catch up)
				} else if (leftOver > 0) { // get the last bit of leftOver
					waitForTick((long) leftOver, (int) ((leftOver % 1) * 1000000));
					leftOver = 0;
				} else {// business as usual
					waitForTick((long) timeLeftonTick, (int) ((timeLeftonTick % 1) * 1000000));
				}
			}
		}
		private synchronized void waitForTick(long milisecond, int nanosecond) {
			try {
				this.wait(milisecond, nanosecond);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}



	private static void sendStateToAll() {
		for (PlayerConn conn : playerConns) {
			sendState(conn);
		}
	}

	private static void sendState(PlayerConn conn) {
		if (conn != null && !conn.socket().isClosed()) {

			ObjectOutputStream out = conn.objectToClient();
			try {
				out.reset();
				out.writeObject(gameLogic.getState());
				out.flush();
			} catch (IOException e) {
				e.printStackTrace(); // Håndter afbrudte forbindelser her
				try {
					conn.socket().close();
					for (int i = 0; i < playerConns.length; i++) {
						if (playerConns[i] != null && playerConns[i].equals(conn)) {
							playerConns[i] = null;
							gameLogic.
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

		}
	}



	private static void playerJoins() {
		while (!joinQueue.isEmpty()) {
			PlayerConn conn = joinQueue.poll();
			try {
				Player p = gameLogic.makePlayer(conn.name());
				conn.outToClient().writeBytes("tilmeldt " + p.getName() + " " + p.getLocation().getX() + " " + p.getLocation().getY() + "\n");
				conn.outToClient().flush();
				int i = sizeOfConns();
				playerConns[i] = conn;

				System.out.println("Tilmeldt " + conn.name() + " som spiller " + i);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static int sizeOfConns() {
		int size = 0;
			for (PlayerConn connection : playerConns) {
				if (connection != null) {
					size++;
				}
			}
		return size;
	}

}
