package spil.version1.server;

import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;


public class Server {

	static Queue<String> actions = new PriorityQueue<>();

	static ConcurrentArrayList players = new ConcurrentArrayList();
	static Socket[] connections = new Socket[5];
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		new JoinThread().start();



	}

	private static class gameTickThread extends Thread {
		public void run() {
			while (true) {
				long ting = System.nanoTime();
				GameLogic.movePlayers(actions);
				try {
					Thread.sleep(8-(ting/1_000_000));
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
					if (connections.length >= 5) {
						connectionSocket.close();
					} else {
						int i = sizeOfSockets();
						connections[i] = connectionSocket;
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
						String message = read.readLine();

						GameLogic.makePlayer(message.split(" ")[2]);
						new ServerThread(connectionSocket, actions).start();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}
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

}
