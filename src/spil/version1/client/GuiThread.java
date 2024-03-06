package spil.version1.client;

import javafx.application.Application;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Gui;
import spil.version1.gamefiles.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

;

public class GuiThread extends Thread{
	public static Player me;
	public void run(){
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Indtast spillernavn");
		String navn = null;
		try {
			navn = inFromUser.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		me= GameLogic.makePlayer(navn);

		Application.launch(Gui.class);
	}
}
