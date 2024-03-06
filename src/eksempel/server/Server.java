package eksempel.server;

import java.net.ServerSocket;
import java.net.Socket;
public class Server {
	
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		common c = new common("eksempel");
		ServerSocket welcomeSocket = new ServerSocket(6789);
		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			(new ServerThread(connectionSocket,c)).start();
		}
	}

}
