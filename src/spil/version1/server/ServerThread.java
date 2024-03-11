package spil.version1.server;
import java.net.*;
import java.io.*;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public class ServerThread extends Thread{
	Socket[] sockets;

	Queue<String> actions;
	BufferedReader[] inFromclients;

	public ServerThread(Socket[] sockets, BufferedReader[] inFromclients,Queue<String> actions) {
		this.sockets = sockets;
		this.actions = actions;
		this.inFromclients = inFromclients;
	}
	public void run() {
		try {
			for (int i = 0; i < sockets.length; i++) {
				Socket socket = sockets[i];
				if (socket == null) {
					continue;
				}
				BufferedReader inFromClient = inFromclients[i];
				String clientSentence = "";
				if (inFromClient.ready()) {
					clientSentence = inFromClient.readLine();
					System.out.println("Received: " + clientSentence);
				}

				if (clientSentence.startsWith("arnold ")) {
					actions.add(clientSentence);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
