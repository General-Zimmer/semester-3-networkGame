package spil.version1.client;

import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;
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
			clientSocket = new Socket("10.10.137.90",1337);
			objectOutToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			objectInFromServer = new ObjectInputStream(clientSocket.getInputStream());
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
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


		while(true){
			if (readBoardFromServer()) {
				updateLocalBoard();
			}


			Thread.sleep(8);
		}
	}


	public static boolean readBoardFromServer() {
		List<Player> playersList;


		try {
			playersList = (List<Player>) objectInFromServer.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

		//System.out.println("Antal spillere: " + playersList.size());
		System.out.println(playersList.toString());
        localLogic.players = playersList;
		return true;
	}

	public static void sendMoveToServer(String move) {
		System.out.println("SENDTE ET MOVE :D");
		try {
			outToServer.writeBytes("arnold " + navnGlobal+ " " + move+ "\n");
			outToServer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void updateLocalBoard(){

		for(int i = 0; i < localLogic.players.size();  i++) {
			Player p = localLogic.players.get(i);
			localLogic.updatePlayer(p, 0, 0, localLogic.players.get(i).getDirection());

			//System.out.println("Spiller: " + p.getName() + ", X: " + p.getXpos() + ", Y: " + p.getYpos());
		}
	}

	public static Player getME() {
		for (Player p : localLogic.players) {
			if(p.getName().equals(navnGlobal)) return p;
		}
		return null;
	}
}


