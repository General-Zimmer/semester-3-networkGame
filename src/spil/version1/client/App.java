package spil.version1.client;

import javafx.application.Application;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Gui;
import spil.version1.gamefiles.Player;
import spil.version1.interfaces.IEGameLogic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

;

public class App {
	public static Player me;
	public static IEGameLogic gameLogic;
	public static void main(String[] args) throws Exception{	
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Indtast spillernavn");
		gameLogic = new GameLogic();
		String navn = inFromUser.readLine();

		me = gameLogic.makePlayer(navn);
		gameLogic.makeVirtualPlayer(); // to be removed
		Application.launch(Gui.class);
	}
}
