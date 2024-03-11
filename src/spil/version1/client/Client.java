package spil.version1.client;

import javafx.application.Platform;
import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Gui;
import spil.version1.gamefiles.Player;
import spil.version1.interfaces.IEGameLogic;

import java.io.*;
import java.net.Socket;
import java.util.List;

// Denne er kun medtaget til Test-formål, skal IKKE anvendes.
public class Client{
	public static String navnGlobal;
	public static GameLogic localLogic = new GameLogic();

	private static final BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	private static Socket clientSocket;
	private static  DataOutputStream outToServer;
	private static BufferedReader inFromServer;

	static ObjectInputStream objectInFromServer;
	static ObjectOutputStream objectOutToServer;

	public static void main(String argv[]) throws Exception{
		GuiThread gui = new GuiThread();
		gui.start();

		System.out.println("Indtast spillernavn");
		String navn = inFromUser.readLine();
		navnGlobal = navn;


		try {
			clientSocket = new Socket("10.10.131.68",1337);
			objectOutToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			objectInFromServer = new ObjectInputStream(clientSocket.getInputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


		outToServer.writeBytes("arnold tilmeld "+ navn+"\n");

		boolean wait = true;
		while(wait){//vente på svar fra server
			String message = inFromServer.readLine();

			if(message.startsWith("tilmeldt " + navnGlobal)){
				wait=false;
			}else if(message.equals(navn+ " afvist")){
				return;
			}
		}


		while(true) {
			if (readBoardFromServer()) {
				updateLocalBoard(); // Opdater GUI baseret på den modtagne spiltilstand
			}
			Thread.sleep(8); // Justér dette tal baseret på dit behov
		}


		}



	public static boolean readBoardFromServer() {
		try {
			List<Player> playersList = (List<Player>) objectInFromServer.readObject();
			localLogic.players = playersList; // Opdaterer spillerlisten
			return true;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void sendMoveToServer(String move) {
		System.out.println("SENDTE ET MOVE :D");
		try {
			outToServer.writeBytes("arnold " + navnGlobal+ " " + move+ "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void updateLocalBoard() {
		Platform.runLater(() -> {
			// Rens GUI før opdatering
			Gui.clearBoard();

			// Opdater GUI med hver spillers nuværende position
			for (Player player : localLogic.players) {
				Gui.placePlayerOnScreen(player.getLocation(), player.getDirection(), player);
			}
		});
	}

	public static Player getME() {
		for (Player p : localLogic.players) {
			if(p.getName().equals(navnGlobal)) return p;
		}
		return null;
	}
}


