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
				throw new RuntimeException(e);
			}
		}

		public void run() {
			while (true) {
				Socket connectionSocket;
				try {
					connectionSocket = welcomeSocket.accept();
					if (sizeOfConns() + joinQueue.size() >= 5) {
						connectionSocket.close();
					} else {

						ObjectOutputStream objectToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
						DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
						BufferedReader read = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
						String name;
						try {
							name = read.readLine().split(" ")[2];
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						PlayerConn playerConn = new PlayerConn(connectionSocket, outToClient, objectToClient, read, name);

						System.out.println("queued player: " + name + " up");
						joinQueue.add(playerConn);
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
			synchronized (this) {
				while (true) {
					double beforeTime = System.nanoTime();
					playerJoins();
					gameLogic.movePlayers(actions);
					sendBytesBack();

					double timeLeftonTick = msPerTick - (System.nanoTime() - beforeTime) / 1000000.0;

					try {
						if (timeLeftonTick > 0) {
							if (leftOver > 0 && leftOver < msPerTick) {
								leftOver -= msPerTick;
							} else if (leftOver > 0) {
								this.wait((long) leftOver, (int) (leftOver % 1));
								leftOver = 0;
							} else
								this.wait((long) timeLeftonTick, (int) (timeLeftonTick % 1));

						} else{
							System.out.println("Server is running behind");
							System.out.println("   timeLeftonTick: " + timeLeftonTick);
							System.out.println("   leftOver: " + leftOver);
							leftOver += timeLeftonTick;
						}
					} catch(InterruptedException e){
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private static void sendBytesBack() {
        for (PlayerConn conn : playerConns) {
            if (conn != null && !conn.socket().isClosed()) {

                ObjectOutputStream out = conn.objectToClient();
                try {
					out.reset();
                    out.writeObject(gameLogic.getState());
                } catch (IOException e) {
                    e.printStackTrace(); // Håndter afbrudte forbindelser her
					try {
						conn.socket().close();
					} catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
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
				int i = sizeOfConns();
				playerConns[i] = conn;
				System.out.println("Tilmeldt " + conn.name() + " som spiller " + i);

			} catch (IOException e) {
				throw new RuntimeException(e);
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
