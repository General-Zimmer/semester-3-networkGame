package spil.version1.client;

import javafx.application.Platform;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.GameState;
import spil.version1.gamefiles.Gui;
import spil.version1.gamefiles.Player;

import java.io.*;
import java.net.Socket;

// Denne er kun medtaget til Test-formål, skal IKKE anvendes.
public class Client{
	public static String navnGlobal;
	public static GameLogic localLogic = new GameLogic(true);

	private static final BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	private static Socket clientSocket;
	private static DataOutputStream outToServer;
	static ObjectInputStream objectInFromServer;

	public static void main(String[] argv) throws Exception {
		GuiThread gui = new GuiThread();
		gui.start();

		System.out.println("Indtast spillernavn");
		String navn = inFromUser.readLine();
		navnGlobal = navn;

		BufferedReader inFromServer;
		try {
			clientSocket = new Socket("localhost", 1337);
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			objectInFromServer = new ObjectInputStream(clientSocket.getInputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Tilsluttet server");

		outToServer.writeBytes("arnold tilmeld " + navn + "\n");


		String message = inFromServer.readLine();

		if (message.startsWith("tilmeldt " + navnGlobal)) {
			System.out.println("Tilmeldt server");
		} else if (message.equals(navn + " afvist")) {
			return;
		}
		localLogic.getState().setTickID(-1); // Sætter tickID til -1, således at spiltilstanden opdateres ved første modtagelse

		while (true) {
			if (readBoardFromServer()) {
				updateLocalBoard(); // Opdater GUI baseret på den modtagne spiltilstand
			}
		}

	}

	public static boolean readBoardFromServer() {
		try {
			GameState gameState = (GameState) objectInFromServer.readObject();
			if (gameState.getTickID() == localLogic.getTickID()) return false; // Hvis spiltilstanden er uændret, returnér false (ingen opdatering af GUI
			System.out.println("Modtog spiltilstand fra server");
			localLogic.setState(gameState); // Opdaterer spillerlisten
			return true;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void sendMoveToServer(String move) {
		try {
			outToServer.writeBytes("arnold " + navnGlobal+ " " + move+ "\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("SENDTE ET MOVE :D");
	}

	public static void updateLocalBoard() {
		Platform.runLater(() -> {
			// Rens GUI før opdatering
			Gui.clearBoard();
			Gui.gui.updateScoreTable();
			// Opdater GUI med hver spillers nuværende position
			for (Player player : localLogic.getPlayers()) {
				Gui.placePlayerOnScreen(player.getLocation(), player.getDirection(), player);
			}
		});
	}

	public static Player getME() {
		for (Player p : localLogic.getPlayers()) {
			if(p.getName().equals(navnGlobal)) return p;
		}
		return null;
	}
}


