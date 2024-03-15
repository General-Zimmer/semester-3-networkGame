package spil.version1.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Queue;

public class ServerThread extends Thread{
	Queue<String> actions;
	PlayerConn[] playerConns;

	public ServerThread(PlayerConn[] playerConns, Queue<String> actions) {
		this.actions = actions;
		this.playerConns = playerConns;
	}
	public void run() {
		try {
			while (true) {
                synchronized (Server.connLock) {
                    for (PlayerConn playerConn : playerConns) {

                        if (playerConn == null) {
                            continue;
                        }
                        BufferedReader inFromClient = playerConn.stringFromClient();
                        String clientSentence = "";
                        if (inFromClient.ready()) {
                            clientSentence = inFromClient.readLine();
                            System.out.println("Received: " + clientSentence);
                        }

                        if (clientSentence.startsWith("arnold ")) {
                            actions.add(clientSentence);
                        }
                    }
                }
                synchronized (this) {
                    wait(0, 500000); // Så vi ikke bruger 100% af en core
                }
            }
        } catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
