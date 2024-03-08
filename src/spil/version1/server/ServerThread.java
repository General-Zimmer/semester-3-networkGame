package spil.version1.server;
import java.net.*;
import java.io.*;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public class ServerThread extends Thread{
	Socket connSocket;

	Queue<String> actions;

	public ServerThread(Socket connSocket, Queue<String> actions) {
		this.connSocket = connSocket;
		this.actions = actions;
	}
	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connSocket.getOutputStream());

			
			String clientSentence = inFromClient.readLine();

			if (clientSentence.startsWith("arnold ")) {
				actions.add(clientSentence);
			} else {
				outToClient.writeBytes("Invalid command\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
