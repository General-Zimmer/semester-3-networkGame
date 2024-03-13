package spil.version1.server;

import spil.version1.gamefiles.Player;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;



public class Server {

	static Queue<String> actions = new PriorityQueue<>();
	static ServerGameLogic gameLogic = new ServerGameLogic();
	static Socket[] sockets = new Socket[5];
	static DataOutputStream[]	objectToClient = new DataOutputStream[5];
	static BufferedReader[] stringFromClients = new BufferedReader[5];
	static Queue<String> actionToSend = new PriorityQueue<>();
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		new JoinThread().start();

		new gameTickThread().start();

		new ServerThread(sockets, stringFromClients, actions).start();

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
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

						String message = read.readLine();
						Player p;
						p = gameLogic.makePlayer(message.split(" ")[2]);

						outToClient.writeBytes("tilmeldt " + p.getName() + " " + p.getLocation().getX() + " " + p.getLocation().getY() + "\n");
						objectToClient[i] = outToClient;
						stringFromClients[i] = read;
						sockets[i] = connectionSocket;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
		}
	}


	private static class gameTickThread extends Thread {
		public void run() {
			double leftOver = 0;
			double msPerTick = 8;
			while (true) {
				double beforeTime = System.nanoTime();
				gameLogic.movePlayers(actions, actionToSend);
				sendBytesBack();
				try {


					double timeLeftonTick = msPerTick - (System.nanoTime() - beforeTime) / 1_000_000;
					if (timeLeftonTick > 0) {
						if (leftOver > 0 && leftOver < msPerTick) {
							leftOver -= msPerTick;
						} else if (leftOver > 0) {
							synchronized (this) {
								this.wait((long) leftOver);
							}
							leftOver = 0;
						} else
							synchronized (this) {
								this.wait((long) timeLeftonTick);
							}
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

	private static void sendBytesBack() {
		for (int i = 0; i < sockets.length; i++) {
			Socket s = sockets[i];
			if (s != null && !s.isClosed()) {

				DataOutputStream out = objectToClient[i];
				int size = actionToSend.size();

				try {
					out.writeBytes(size + "\n");
					while (!actionToSend.isEmpty()) {
						out.writeBytes(actionToSend.poll() + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace(); // Håndter afbrudte forbindelser her
					try {
						s.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
