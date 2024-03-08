package spil.version1.client;

import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Player;
import spil.version1.interfaces.IEGameLogic;

import java.io.*;
import java.net.Socket;

// Denne er kun medtaget til Test-formål, skal IKKE anvendes.
public class Client{
	public static Player me;
	public static String navnGlobal;
	public static ConcurrentArrayList serverBoard = null;
	public static GameLogic localLogic = new GameLogic();

	private static final BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	private static Socket clientSocket;
	private static  DataOutputStream outToServer;
	private static BufferedReader inFromServer;

	static ObjectInputStream objectInFromServer;
	static ObjectOutputStream objectOutToServer;

	public static void main(String argv[]) throws Exception{

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


		outToServer.writeBytes("arnold tilmed "+ navn+"\n");

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

		GuiThread gui = new GuiThread();
		gui.start();
		System.out.println("gui åbenet");

		me= localLogic.makePlayer(navn);

		System.out.println("ind i uendelig loop");
		while(true){
			if (readBoardFromServer()) {
				updateLocalBoard();
			}


			Thread.sleep(8);
		}
	}


	public static boolean readBoardFromServer() {
		ConcurrentArrayList playersList;


		System.out.println("læsr board nu");
		try {
			playersList = (ConcurrentArrayList) objectInFromServer.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

		System.out.println("board læst");
        serverBoard = playersList;
		return true;
	}

	public static void sendMoveToServer(String move) {
		try {
			outToServer.writeBytes("arnold " + navnGlobal+ " " + move+ "\n");
			outToServer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void updateLocalBoard(){
		//TODO: opdater localGameLogic boarded, med gameLogics's updatePlayer metode...
		for(int i = 0; i < serverBoard.size(); i++) {
			if(localLogic.getPlayer(serverBoard.asArrayList().get(i).getName()) == null) {
				Player playerServer = serverBoard.asArrayList().get(i);
				Player playerNew = localLogic.makePlayer(serverBoard.asArrayList().get(i).getName());
				playerNew.setLocation(playerServer.getLocation());
			}
			int deltaX = localLogic.players.get(i).getXpos() - serverBoard.asArrayList().get(i).getXpos();
			int deltaY = localLogic.players.get(i).getYpos() - serverBoard.asArrayList().get(i).getYpos();
			localLogic.updatePlayer(serverBoard.asArrayList().get(i), deltaX, deltaY, serverBoard.asArrayList().get(i).getDirection());



		}
	}
}


