package spil.version1.server;
import java.net.*;
import java.io.*;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public class ServerThread extends Thread{
	Socket[] sockets;

	Queue<String> actions;

	public ServerThread(Socket[] sockets, Queue<String> actions) {
		this.sockets = sockets;
		this.actions = actions;
	}
	public void run() {
		try {
			for (Socket socket : sockets) {
				if (socket == null) {
					continue;
				}
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
				String clientSentence = "";
				if (inFromClient.ready()) {
					clientSentence = inFromClient.readLine();
					System.out.println("Received: " + clientSentence);
				}

				if (clientSentence.startsWith("arnold ")) {
					actions.add(clientSentence);
				} else {
					outToClient.writeBytes("Invalid command\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
