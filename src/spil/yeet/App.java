package spil.yeet;

import java.net.*;
import java.io.*;
import javafx.application.Application;;

public class App {
	public static Player me;
	public static void main(String[] args) throws Exception{	
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Indtast spillernavn");
		String navn = inFromUser.readLine();
		me= GameLogic.makePlayer(navn);
		GameLogic.makeVirtualPlayer(); // to be removed
		Application.launch(Gui.class);
	}
}
