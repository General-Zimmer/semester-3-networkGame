package eksempel.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class ServerThread extends Thread{
	Socket connSocket;
	common c;
	
	public ServerThread(Socket connSocket, common c) {
		this.connSocket = connSocket;
		this.c=c; // Til Web-server opgaven skal denne ikke anvendes
	}
	public void run() {
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connSocket.getOutputStream());
			
			// Do the work and the communication with the client here	
			// The following two lines are only an example
			
			String clientSentence = inFromClient.readLine();
			outToClient.writeBytes("Hej"+ c.getTekst() + '\n' );
		
		} catch (IOException e) {
			e.printStackTrace();
		}		
		// do the work here
	}
}
