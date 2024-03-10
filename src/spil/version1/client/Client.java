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
	public static List<Player> serverBoard = null;
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
			clientSocket = new Socket("10.10.137.219",1337);
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
				System.out.println("Server afviste spiller");
				return;
			}
		}

		System.out.println("stoppet med at vente");

//		String stringRead = inFromServer.readLine();
//		FileInputStream inputStream = new FileInputStream(stringRead);
//		ObjectInputStream objectMap = new ObjectInputStream(inputStream);
//		ConcurrentArrayList playersList = (ConcurrentArrayList) objectMap.readObject();


		System.out.println("gui åbenet");

		System.out.println("ind i uendelig loop");
		while(true){
			if (readBoardFromServer()) {
				updateLocalBoard();
			}


			Thread.sleep(8);
		}
	}


	public static boolean readBoardFromServer() {
		List<Player> playersList;


		System.out.println("læser board nu");
		try {
			playersList = (List<Player>) objectInFromServer.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

		System.out.println("board læst " + playersList.size());
        serverBoard = playersList;
		return true;
	}

	public static void sendMoveToServer(String move) {
		System.out.println("sendte et move");
		try {
			outToServer.writeBytes("arnold " + navnGlobal+ " " + move+ "\n");
			outToServer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

//	public static void updateLocalBoard2(){
//		//TODO: opdater localGameLogic boarded, med gameLogics's updatePlayer metode...
//		System.out.println("TESTLSEKTJL: " + serverBoard.size());
//		for(int i = 0; i < serverBoard.size(); i++) {
//			if(localLogic.getPlayer(serverBoard.asArrayList().get(i).getName()) == null) {
//				Player playerServer = serverBoard.asArrayList().get(i);
//				Player playerNew = localLogic.makePlayer(serverBoard.asArrayList().get(i).getName());
//				playerNew.setLocation(playerServer.getLocation());
//				System.out.println("ny spiller i spil");
//			}
//			int deltaX = localLogic.players.get(i).getXpos() - serverBoard.asArrayList().get(i).getXpos();
//			int deltaY = localLogic.players.get(i).getYpos() - serverBoard.asArrayList().get(i).getYpos();
//			localLogic.updatePlayer(serverBoard.asArrayList().get(i), deltaX, deltaY, serverBoard.asArrayList().get(i).getDirection());
//			System.out.println("spillr opdateret i gui");
//		}
//	}
	public static void updateLocalBoard(){
		localLogic.players = serverBoard;
		//TODO: opdater localGameLogic boarded, med gameLogics's updatePlayer metode...
		System.out.println("TESTLSEKTJL: " + serverBoard.size());
		for(int i = 0; i < localLogic.players.size();  i++) {
			localLogic.updatePlayer(localLogic.players.get(i), 0, 0, serverBoard.get(i).getDirection());
			System.out.println("spiller opdateret i gui");
		}
	}

	public static Player getME() {
		for (Player p : serverBoard) {
			if(p.getName().equals(navnGlobal)) return p;
		}
		return null;
	}
}


