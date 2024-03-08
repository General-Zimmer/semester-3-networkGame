package spil.version1.client;

import spil.version1.gamefiles.ConcurrentArrayList;
import spil.version1.gamefiles.GameLogic;
import spil.version1.gamefiles.Player;

import java.io.*;
import java.net.Socket;

// Denne er kun medtaget til Test-formål, skal IKKE anvendes.
public class Client{
	public static Player me;
	public static ConcurrentArrayList serverBoard = null;
	public static GameLogic localLogic = new GameLogic();

	private static final BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
	private static final Socket clientSocket;


	static {
		try {
			clientSocket = new Socket("localhost",1337);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final DataOutputStream outToServer;

	static {
		try {
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final BufferedReader inFromServer;

	static {
		try {
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String argv[]) throws Exception{
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
		ConcurrentArrayList playersList = (ConcurrentArrayList) objectMap.readObject();

		serverBoard = playersList;

		GuiThread gui = new GuiThread();
		gui.start();

		me= localLogic.makePlayer(navn);

		while(true){
			sendBoardToServer();

			readBoardFromServer();

			updateLocalBoard();

			Thread.sleep(16);
		}
	}


	public static void readBoardFromServer() {
		String stringRead = null;
		FileInputStream inputStream = null;
		ObjectInputStream objectMap = null;
		ConcurrentArrayList playersList = null;

		try {
			stringRead = inFromServer.readLine();
			inputStream = new FileInputStream(stringRead);
			objectMap = new ObjectInputStream(inputStream);
			playersList = (ConcurrentArrayList) objectMap.readObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        GameLogic.players = playersList.asArrayList();
	}

	public static void sendBoardToServer() throws IOException {
		//TODO: send boarded
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		ObjectOutputStream serializeStream = new ObjectOutputStream(output);

		serializeStream.writeObject(localLogic.players);

		outToServer.writeBytes(output.toString()+"\n");
	}

	public static void updateLocalBoard(){
		//TODO: opdater localGameLogic boarded, med gameLogics's updatePlayer metode...
		for(int i = 0; i < serverBoard.size(); i++) {
			int deltaX = localLogic.players.get(i).getXpos() - serverBoard.asArrayList().get(i).getXpos();
			int deltaY = localLogic.players.get(i).getYpos() - serverBoard.asArrayList().get(i).getYpos();
			localLogic.updatePlayer(serverBoard.asArrayList().get(i), deltaX, deltaY, serverBoard.asArrayList().get(i).getDirection());

		}
	}
}


