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
	static ObjectOutputStream[]	objectToClient = new ObjectOutputStream[5];
	static BufferedReader[] stringFromClients = new BufferedReader[5];
	static final Object lock = new Object();
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
						ObjectOutputStream objectOutToServer = new ObjectOutputStream(connectionSocket.getOutputStream());
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

						String message = read.readLine();
						Player p;
						synchronized (lock){
							p = gameLogic.makePlayer(message.split(" ")[2]);
						}
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


	private static class gameTickThread extends Thread {
		public void run() {
			double leftOver = 0;
			double msPerTick = 30;
			synchronized (this) {
				while (true) {
					double beforeTime = System.nanoTime();
					gameLogic.movePlayers(actions);
					try {
						sendBytesBack();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					try {


						double timeLeftonTick = msPerTick - (System.nanoTime() - beforeTime) * 0.000_0001;

						if (timeLeftonTick > 0) {
							if (leftOver > 0 && leftOver < msPerTick) {
								leftOver -= msPerTick;
							} else if (leftOver > 0) {
								this.wait((long) leftOver);
								leftOver = 0;
							} else
								this.wait((long) timeLeftonTick);

						} else{
							System.out.println("Server is running behind: " + timeLeftonTick + " and " + leftOver);
							leftOver += timeLeftonTick;
						}
						System.out.println("Leftover: " + leftOver);
						System.out.println("Timeleft: " + timeLeftonTick);
					} catch(InterruptedException e){
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private static void sendBytesBack() throws IOException {
		for (int i = 0; i < sockets.length; i++) {
			Socket s = sockets[i];
			if (s != null && !s.isClosed()) {

				ObjectOutputStream out = objectToClient[i];
				try {
					out.reset();
					out.writeObject(gameLogic.getPlayers());
				} catch (IOException e) {
					e.printStackTrace(); // Håndter afbrudte forbindelser her
					s.close();
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
