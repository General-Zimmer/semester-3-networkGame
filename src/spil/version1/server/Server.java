package spil.version1.server;

import spil.version1.gamefiles.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.PriorityQueue;
import java.util.Queue;



public class Server {

	static Queue<String> actions = new PriorityQueue<>();
	static ServerGameLogic gameLogic = new ServerGameLogic();
	static Socket[] sockets = new Socket[5];
	static ObjectOutputStream[]	objectToClient = new ObjectOutputStream[5];
	static BufferedReader[] stringFromClients = new BufferedReader[5];
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		new JoinThread().start();

		new gameTickThread().start();

		new ServerThread(sockets, stringFromClients, actions).start();

	}

	private static class gameTickThread extends Thread {
		public void run() {
			double leftOver = 0;
			double msPerTick = 8;
			while (true) {
				double beforeTime = System.nanoTime();
				gameLogic.movePlayers(actions);
				try {
					sendBytesBack();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				try {

					double timeLeftonTick = msPerTick - (System.nanoTime() - beforeTime) / 1_000_000;
					if (timeLeftonTick > 0) {
						if (leftOver > 0 && leftOver < msPerTick) {
							leftOver -= msPerTick;
						} else if (leftOver > 0) {
							Thread.sleep((long) leftOver);
							leftOver = 0;
						} else
							Thread.sleep((long) timeLeftonTick);
					} else {
						System.out.println("Server is running behind: " + timeLeftonTick + " and " + leftOver);
						leftOver += timeLeftonTick;
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private static class JoinThread extends Thread {
		ServerSocket welcomeSocket = new ServerSocket(1337);

        private JoinThread() throws IOException {
        }

        public void run() {
			while (true) {
				Socket connectionSocket;
				try {
					connectionSocket = welcomeSocket.accept();
					if (sizeOfSockets() >= 5) {
						connectionSocket.close();
					} else {

						int i = sizeOfSockets();
						DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						ObjectOutputStream objectOutToServer = new ObjectOutputStream(connectionSocket.getOutputStream());
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

						String message = read.readLine();

						connectionSocket.setSoTimeout(300);
						Player p = gameLogic.makePlayer(message.split(" ")[2]);
						outToClient.writeBytes("tilmeldt " + p.getName() + " " + p.getLocation().getX() + " " + p.getLocation().getY() + "\n");
						objectToClient[i] = objectOutToServer;
						stringFromClients[i] = read;
						sockets[i] = connectionSocket;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
		}
	}

	private static void sendBytesBack() throws IOException {
		int i = 0;
		try {
			for (; i < sockets.length; i++) {
				Socket s = sockets[i];
				ObjectOutputStream outToClient = objectToClient[i];
				if (s == null) {
					continue;
				}

				outToClient.writeObject(gameLogic.getPlayers());
				System.out.println(gameLogic.getPlayers().toString());
			}
		} catch (IOException ex) {
			if (ex instanceof SocketException) {
				System.out.println("Client disconnected");
				sockets[i] = null;
				objectToClient[i] = null;
			} else
				ex.printStackTrace();
		}
	}

	private static class getActionsThread extends Thread {
		public void run() {
			while (true) {
				try {
					for (Socket connection : sockets) {
						if (connection == null) {
							continue;
						}
						BufferedReader inFromUser = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						if (inFromUser.ready()) {
							String sentence = inFromUser.readLine();
							actions.add(sentence);
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}


	private static int sizeOfSockets() {
		int size = 0;
		for (Socket connection : sockets) {
			if (connection != null) {
				size++;
			}
		}
		return size;
	}

}
