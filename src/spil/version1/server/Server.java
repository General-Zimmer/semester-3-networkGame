package spil.version1.server;

import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Player;
import spil.version1.interfaces.IEGameLogic;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;


public class Server {

	static Queue<String> actions = new PriorityQueue<>();
	static IEGameLogic gameLogic = new ServerGameLogic();
	static ConcurrentArrayList players = new ConcurrentArrayList();
	static Socket[] connections = new Socket[5];
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		new JoinThread().start();

		new gameTickThread().start();


	}

	private static class gameTickThread extends Thread {
		public void run() {
			while (true) {
				long ting = System.nanoTime();
				gameLogic.movePlayers(actions);
				try {
					sendBytesBack();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				long t = System.nanoTime() - ting;
				System.out.println(t / 1_000_000 + " ms");
				try {
					Thread.sleep(500 - t / 1_000_000);
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
						connections[i] = connectionSocket;
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

						DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						String message = read.readLine();

						Player p = gameLogic.makePlayer(message.split(" ")[2]);
						outToClient.writeBytes("tilmeldt " + p.getName() + " " + p.getLocation().getX() + " " + p.getLocation().getY() + "\n");
						new ServerThread(connectionSocket, actions).start();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
		}
	}

	private static void sendBytesBack() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(players);

			byte[] bytes = bos.toByteArray();
			for (Socket s: connections){
				if (s == null) {
					continue;
				}
				System.out.println("Sending bytes to client");
				DataOutputStream outToClient = new DataOutputStream(s.getOutputStream());
				outToClient.write(bytes);
				System.out.println("Players object serialized. ");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	private static int sizeOfSockets() {
		int size = 0;
		for (Socket connection : connections) {
			if (connection != null) {
				size++;
			}
		}
		return size;
	}
	// hjælpemetode
	static byte[] serialize(final Object obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(obj);
			out.flush();
			return bos.toByteArray();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
