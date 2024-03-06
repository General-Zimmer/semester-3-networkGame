package spil.version1.client;

import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Denne er kun medtaget til Test-formål, skal IKKE anvendes.
public class Client {
	public static Player me;
	public static final GameLogic localLogic = new GameLogic();
	public static void main(String argv[]) throws Exception{
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Indtast spillernavn");
		String navn = null;
		navn = inFromUser.readLine();

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


