package spil.version1.client;

import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Player;

import java.io.*;
import java.net.Socket;

// Denne er kun medtaget til Test-formål, skal IKKE anvendes.
public class Client{
	public static Player me;
	public static final GameLogic localLogic = new GameLogic();
	public static void main(String argv[]) throws Exception{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		Socket clientSocket= new Socket("localhost",1337);
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		System.out.println("Indtast spillernavn");
		String navn = null;
		navn = inFromUser.readLine();

		outToServer.writeBytes("arnold tilmed "+ navn+"\n");

		boolean wait = true;
		while(wait){//vente på svar fra server
			String message = inFromServer.readLine();

			if(message.equals(navn+ " tilmeldt")){
				wait=false;
			}else if(message.equals(navn+ " afvist")){
				System.out.println("Server afviste spiller");
				return;
			}
		}

		String stringRead = inFromServer.readLine();
		FileInputStream inputStream = new FileInputStream(stringRead);

		ObjectInputStream objectMap = new ObjectInputStream(inputStream);

		ConcurrentArrayList gameState = (ConcurrentArrayList) objectMap.readObject();

		GuiThread gui = new GuiThread();
		gui.start();

		me= localLogic.makePlayer(navn);

		System.out.println("test");

//		String sentence;
//		String modifiedSentence;
//		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
//		Socket clientSocket= new Socket("localhost",6789);
//		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
//		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//		sentence = inFromUser.readLine();
//		outToServer.writeBytes(sentence + '\n');
//		modifiedSentence = inFromServer.readLine();
//		System.out.println("FROM SERVER: " + modifiedSentence);
//		clientSocket.close();
	}
}


